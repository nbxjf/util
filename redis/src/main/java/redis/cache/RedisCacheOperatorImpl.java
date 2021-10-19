package redis.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import redis.cache.RedisCacheLoader.SingleLoader;
import redis.common.RedisKeyType;
import redis.common.RedisMap;
import redis.common.RedisValueInstance;
import redis.common.SafeRedisValueType.ExceptionalValue;
import redis.lock.RedisBatchLock;
import redis.lock.RedisLockService;
import redis.pool.RedisPool;
import utils.serializer.Serializer;

/**
 * Created by Jeff_xu on 2021/9/29.
 *
 * @author Jeff_xu
 */
@Slf4j
public class RedisCacheOperatorImpl<K, V> implements RedisCacheOperator<K, V> {

    private final RedisMap<K, CacheEntity> redisMap;
    /**
     * 用于异步刷新缓存的线程池
     */
    private final ExecutorService asyncLoadExecutor;
    /**
     * redis缓存的使用空间的前缀
     */
    private static final String USE_SPACE = "cache";

    private final boolean cacheNullable;
    private final int logicalExpireSeconds;
    private final int physicalExpireSeconds;
    private final RedisLockService lockService;
    private final RedisKeyType<K> keyType;

    private String getMapName(String prefix, String version) {
        StringBuilder mapName = new StringBuilder(USE_SPACE);
        if (StringUtils.isNotBlank(prefix)) {
            mapName.append("/").append(prefix);
        }
        if (StringUtils.isNotBlank(version)) {
            mapName.append("/").append(version);
        }
        return mapName.toString();
    }

    public RedisCacheOperatorImpl(RedisPool redisPool,
                                  String prefix,
                                  String version,
                                  int logicalExpireSeconds,
                                  int physicalExpireSeconds,
                                  boolean cacheNullable,
                                  RedisKeyType<K> keyType,
                                  Serializer serializer) {
        this.redisMap = new RedisMap<>(redisPool, getMapName(prefix, version), keyType, new RedisValueInstance<>(CacheEntity.class, serializer));
        this.cacheNullable = cacheNullable;
        this.logicalExpireSeconds = logicalExpireSeconds;
        this.physicalExpireSeconds = physicalExpireSeconds;
        this.lockService = new RedisLockService(redisPool);
        this.keyType = keyType;

        AtomicLong threadId = new AtomicLong();
        ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("redis-cache-worker-" + threadId.incrementAndGet());
            return thread;
        };
        this.asyncLoadExecutor = new ThreadPoolExecutor(
            1,
            50,
            60,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            threadFactory
        );
    }

    @Override
    public V get(K key) {
        return get(Collections.singletonList(key)).get(key);
    }

    @Override
    public Map<K, V> get(List<K> keys) {
        return null;
    }

    @Override
    public V load(K key, SingleLoader<K, V> redisCacheLoader) {
        return load(Collections.singletonList(key), redisCacheLoader).get(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<K, V> load(List<K> keyList, RedisCacheLoader<K, V> redisCacheLoader) {
        // 优先从redis中获取当前缓存的值
        Map<K, CacheEntity> cachedEntityMap = getCacheEntityFromRedis(keyList);

        Map<K, V> result = new HashMap<>(keyList.size());
        cachedEntityMap.forEach((k, cacheEntity) -> result.put(k, (V)cacheEntity.getValue()));
        {
            // 计算已经失效的缓存，触发异步刷新已经失效的缓存，但是当次访问依然使用该缓存结果
            if (!cachedEntityMap.isEmpty()) {
                List<K> expiredKeys = getExpiredKeys(cachedEntityMap);
                asyncLoadAndRefreshCache(expiredKeys, redisCacheLoader);
            }
        }
        {
            // 同步加载miss的缓存
            List<K> missedKeyList = (List<K>)CollectionUtils.disjunction(keyList, cachedEntityMap.keySet());
            if (!missedKeyList.isEmpty()) {
                Map<K, V> missedCacheEntityMap = syncLoadAndRefreshCache(missedKeyList, redisCacheLoader);
                result.putAll(missedCacheEntityMap);
            }
        }

        return result;
    }

    @Override
    public void refresh(K key, V value) {
        refresh(Collections.singletonMap(key, value));
    }

    @Override
    public void refresh(Map<K, V> keyValues) {
        saveToCache(keyValues);
    }

    @Override
    public void invalid(K key) {
        invalid(Collections.singletonList(key));
    }

    @Override
    public void invalid(List<K> keys) {
        removeCache(keys);
    }

    @Override
    public boolean exists(K key) {
        return redisMap.exist(key);
    }

    /**
     * 从redis中获取缓存的实体
     *
     * @param keys keys
     * @return 缓存的值
     */
    private Map<K, CacheEntity> getCacheEntityFromRedis(List<K> keys) {
        keys = keys.stream().distinct().collect(Collectors.toList());
        Map<K, CacheEntity> result = new HashMap<>(keys.size());
        Map<K, ExceptionalValue<CacheEntity>> cacheEntityList = redisMap.safeValueType().get(keys);
        cacheEntityList.forEach((k, exceptionalValue) -> {
            if (exceptionalValue.isFailed()) {
                log.error("load key:{} cache error", k, exceptionalValue.getException());
            } else {
                result.put(k, exceptionalValue.getValue());
            }
        });
        return result;
    }

    /**
     * 获取已经过期的key列表
     *
     * @param cacheEntityMap 缓存结果
     * @return 已经过期的缓存
     */
    private List<K> getExpiredKeys(Map<K, CacheEntity> cacheEntityMap) {
        if (cacheEntityMap.isEmpty()) {
            return new ArrayList<>();
        }
        long currentTimeMillis = System.currentTimeMillis();

        List<K> expiredKeyList = new ArrayList<>();
        cacheEntityMap.forEach((key, cacheEntity) -> {
            if (cacheEntity.getTimestamp() <= 0 ||
                cacheEntity.getTimestamp() + logicalExpireSeconds > currentTimeMillis ||
                cacheEntity.getTimestamp() + physicalExpireSeconds > currentTimeMillis) {
                expiredKeyList.add(key);
            }
        });
        return expiredKeyList;
    }

    /**
     * 异步刷新缓存
     *
     * @param keyList 待刷新缓存的集合
     */
    private void asyncLoadAndRefreshCache(List<K> keyList, RedisCacheLoader<K, V> redisCacheLoader) {
        if (CollectionUtils.isEmpty(keyList)) {
            return;
        }
        try {
            asyncLoadExecutor.execute(() -> syncLoadAndRefreshCache(keyList, redisCacheLoader));
        } catch (RejectedExecutionException e) {
            log.error("could not async load and save cache for " + keyList.size(), e);
        }
    }

    /**
     * 同步刷新缓存
     *
     * @param keyList 待刷新缓存的集合
     */
    private Map<K, V> syncLoadAndRefreshCache(List<K> keyList, RedisCacheLoader<K, V> redisCacheLoader) {
        if (CollectionUtils.isEmpty(keyList)) {
            return Collections.emptyMap();
        }
        Map<K, V> result = new HashMap<>(keyList.size());
        // 1.解决多个线程同时对某个key进行刷新的问题，先获取分布式锁，由第一个获取到锁的线程触发计算刷新，并写入redis
        RedisBatchLock<K> computingLock = lockService.buildBatchLock("", keyList, keyType, 3000);
        List<K> lockedKeys;
        List<K> unLockedKeys;
        if (computingLock.tryAcquire()) {
            try {
                lockedKeys = computingLock.getLockedKeys();
                unLockedKeys = computingLock.getUnLockedKeys();
                result.putAll(saveToCache(loadCacheValue(lockedKeys, redisCacheLoader)));
            } finally {
                computingLock.release();
            }
        } else {
            lockedKeys = new ArrayList<>();
            unLockedKeys = new ArrayList<>(keyList);
        }

        // 2. 对于获取锁未成功key，表明此时正在被其他线程触发计算并更新缓存中，可以直接从redis中读取缓存，加入到自己的结果中（坐享其成）
        if (!unLockedKeys.isEmpty()) {
            // 等待1秒钟，即允许其他线程在1秒钟内计算出结果值，如果1秒内其他的线程还未释放锁，说明计算还未完成，那么当前线程不再孔灯，主动的去同步获取缓存
            RedisBatchLock<K> waitingLock = lockService.buildBatchLock("", unLockedKeys, keyType, 3000);
            List<K> releasedKeys = waitingLock.releasedKeys(1, TimeUnit.SECONDS);

            List<K> refreshedKeys = new ArrayList<>(releasedKeys.size());
            for (Map.Entry<K, CacheEntity> entry : getCacheEntityFromRedis(releasedKeys).entrySet()) {
                if (entry.getValue() != null) {
                    // 将其他线程的计算结果加入到自己的结果中（坐享其成）
                    //noinspection unchecked
                    result.put(entry.getKey(), (V)entry.getValue().getValue());
                    refreshedKeys.add(entry.getKey());
                }
            }

            // 3. 在某些情况下，例如：
            //    1. 抢占到计算权的线程在计算值时遇到问题，或者
            //    2. 计算所花费的时间超过1秒
            // 因此，对于所有未获得计算权的key，如果我们没有从缓存中得到它们的值，则直接对它们进行计算
            List<K> leftKeys = unLockedKeys
                .stream()
                .filter(k -> !refreshedKeys.contains(k))
                .collect(Collectors.toList());
            if (!leftKeys.isEmpty()) {
                // 3.1 对所有本应由其他线程负责计算却由于某些原因没有完成计算（或没有存入缓存、或缓存读取失败）的key，
                // 我们直接进行计算，并将计算结果保存到缓存中。
                // 由于计算可能会花费一定的时间，在完成计算后，可能其他线程已经提前完成计算并将其保存到缓存中，
                // 我们的计算结果会覆盖它的计算结果。
                // 当然，其他线程的计算结果也有可能会覆盖我们的计算结果，这些情况均可以不作考虑。
                log.info("directly calculate the left keys {}", leftKeys);
                result.putAll(saveToCache(loadCacheValue(lockedKeys, redisCacheLoader)));
            }
        }
        return result;
    }

    /**
     * 从回调中加载缓存对象
     *
     * @param keyList          缓存列表
     * @param redisCacheLoader 加载器
     */
    private Map<K, V> loadCacheValue(List<K> keyList, RedisCacheLoader<K, V> redisCacheLoader) {
        if (keyList.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<K, V> loadValueMap = redisCacheLoader.load(keyList);

        Map<K, V> cacheValueMap = new HashMap<>(keyList.size());
        if (cacheNullable) {
            // 如果允许缓存null值，则遍历所有的key，存入缓存，如果不存在，则存入null
            for (K key : keyList) {
                cacheValueMap.put(key, loadValueMap.get(key));
            }
        } else {
            // 如果不允许缓存null值，则过滤所有的为null的value
            loadValueMap.forEach((k, v) -> {
                if (Objects.nonNull(v)) {
                    cacheValueMap.put(k, v);
                }
            });
        }
        return cacheValueMap;
    }

    /**
     * 将缓存结果保存至redis
     *
     * @param toCacheValues 缓存结果
     */
    private Map<K, V> saveToCache(Map<K, V> toCacheValues) {
        if (toCacheValues.isEmpty()) {
            return Collections.emptyMap();
        }
        long timestamp = System.currentTimeMillis();
        Map<K, CacheEntity> toCacheEntityMap = new HashMap<>(toCacheValues.size());
        toCacheValues.forEach((k, v) -> {
            CacheEntity cacheEntity = new CacheEntity();
            cacheEntity.setValue(v);
            cacheEntity.setTimestamp(timestamp);
            toCacheEntityMap.put(k, cacheEntity);
        });
        // 设置超时时间时，在physicalExpireSeconds基础上增加一个0~1000毫秒的随机数，防止大量的key在同一时刻失效，缓存穿透至db对db造成巨大压力
        redisMap.set(toCacheEntityMap, physicalExpireSeconds * 1000L + ThreadLocalRandom.current().nextLong(0, 1000));
        return toCacheValues;
    }

    /**
     * 删除缓存
     *
     * @param keyList key列表
     */
    private void removeCache(List<K> keyList) {
        if (CollectionUtils.isEmpty(keyList)) {
            return;
        }
        redisMap.del(keyList);
    }
}

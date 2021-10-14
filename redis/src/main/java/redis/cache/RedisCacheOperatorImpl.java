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
import redis.common.RedisValueType;
import redis.common.SafeRedisValueType;
import redis.common.SafeRedisValueType.ExceptionalValue;
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
     * redis缓存值的描述类型
     */
    private final RedisValueType<V> redisValueType;
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
                                  RedisValueType<V> redisValueType,
                                  Serializer serializer) {
        this.redisValueType = redisValueType;
        this.redisMap = new RedisMap<>(redisPool, getMapName(prefix, version), keyType, new RedisValueInstance<>(CacheEntity.class, serializer));
        this.cacheNullable = cacheNullable;
        this.logicalExpireSeconds = logicalExpireSeconds;
        this.physicalExpireSeconds = physicalExpireSeconds;

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
        return null;
    }

    @Override
    public Map<K, V> get(List<K> keys) {
        return null;
    }

    @Override
    public V load(K key, SingleLoader<K, V> redisCacheLoader) {
        return null;
    }

    @Override
    public Map<K, V> load(List<K> key, RedisCacheLoader<K, V> redisCacheLoader) {
        return null;
    }

    @Override
    public void refresh(K key, V value) {

    }

    @Override
    public void refresh(Map<K, V> keyValues) {

    }

    @Override
    public void invalid(K key) {

    }

    @Override
    public void invalid(List<K> keys) {

    }

    @Override
    public boolean exists(K key) {
        return false;
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
     * 获取已经达到逻辑过期时间的key列表
     *
     * @param cacheEntityMap 缓存结果
     * @return 已经物理过期的缓存
     */
    private List<K> getLogicalExpiredKeys(Map<K, CacheEntity> cacheEntityMap) {
        return new ArrayList<>();
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
    private void syncLoadAndRefreshCache(List<K> keyList, RedisCacheLoader<K, V> redisCacheLoader) {
        if (CollectionUtils.isEmpty(keyList)) {
            return;
        }
        saveToCache(loadCacheValue(keyList, redisCacheLoader));
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
            cacheValueMap.putAll(loadValueMap);
        } else {
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
    private void saveToCache(Map<K, V> toCacheValues) {
        if (toCacheValues.isEmpty()) {
            return;
        }
        long timestamp = System.currentTimeMillis();
        Map<K, CacheEntity> toCacheEntity = new HashMap<>(toCacheValues.size());
        toCacheValues.forEach((k, v) -> {
            CacheEntity cacheEntity = new CacheEntity();
            cacheEntity.setValue(v);
            cacheEntity.setTimestamp(timestamp);
            toCacheEntity.put(k, cacheEntity);
        });
        redisMap.set(toCacheEntity, physicalExpireSeconds * 1000L);
    }
}

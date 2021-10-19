package redis.lock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.common.RedisKeyType;
import redis.pool.RedisPool;

/**
 * Created by Jeff_xu on 2021/10/18.
 *
 * @author Jeff_xu
 */
public class RedisBatchLockImpl<K> extends AbstractRedisLock<K> implements RedisBatchLock<K> {

    private final List<K> keys;
    List<K> lockedKeys;
    private static final int ACQUIRE_RETRY_INTERVAL = 100;

    public RedisBatchLockImpl(RedisPool redisPool,
                              RedisKeyType<K> keyType,
                              List<K> keys,
                              long expiryTimeMillis,
                              String scope) {

        super(redisPool, keyType, expiryTimeMillis, scope);
        this.keys = keys;
    }

    @Override
    public boolean tryAcquire() {
        synchronized (this) {
            if (lockedKeys != null) {
                throw new IllegalStateException("already acquired");
            }
        }

        List<K> lks = doAcquire(redisPool.getJedisClient(), keys);
        if (lks.isEmpty()) {
            return false;
        }
        synchronized (this) {
            if (lockedKeys == null) {
                lockedKeys = Collections.unmodifiableList(lks);
                return true;
            }
        }
        // 进入该行说明另一个线程已经acquire，本线程释放并退出，标记为获取失败
        doRelease(redisPool.getJedisClient(), lks);
        return false;
    }

    @Override
    public void release() {
        List<K> copy;
        synchronized (this) {
            if (lockedKeys == null) {
                throw new IllegalStateException("not acquired yet");
            }
            if (lockedKeys.isEmpty()) {
                return;
            }
            copy = lockedKeys;
            // 阻止再次acquire
            lockedKeys = Collections.emptyList();
        }
        try (Jedis jedis = redisPool.getJedisClient()) {
            doRelease(jedis, copy);
        }
    }

    @Override
    public List<K> getLockedKeys() {
        synchronized (this) {
            if (lockedKeys == null) {
                throw new IllegalStateException("not acquired yet");
            }
            // list为只读，可以直接返回
            return lockedKeys;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<K> getUnLockedKeys() {
        return (List<K>)CollectionUtils.disjunction(keys, lockedKeys);
    }

    @Override
    public List<K> releasedKeys(long timeout, TimeUnit timeUnit) {
        long deadline = System.currentTimeMillis() + timeUnit.toMillis(timeout);
        List<K> leftKeys = new ArrayList<>(keys);
        List<K> finishedKeys = new ArrayList<>();
        for (; ; ) {
            try (Jedis jedis = redisPool.getJedisClient()) {
                Pipeline pipeline = jedis.pipelined();
                for (K key : leftKeys) {
                    pipeline.exists(finalKey(key));
                }
                //noinspection unchecked
                List<Boolean> existences = (List<Boolean>)(List<?>)pipeline.syncAndReturnAll();
                for (int i = 0; i < leftKeys.size(); i++) {
                    if (!existences.get(i)) {
                        finishedKeys.add(leftKeys.get(i));
                    }
                }
            }
            leftKeys.removeAll(finishedKeys);
            finishedKeys.clear();

            if (leftKeys.isEmpty()) {
                return Collections.unmodifiableList(keys);
            }

            if (System.currentTimeMillis() + ACQUIRE_RETRY_INTERVAL > deadline) {
                return keys.stream().filter(k -> !leftKeys.contains(k)).collect(Collectors.toList());
            }

            try {
                //noinspection BusyWait
                Thread.sleep(ACQUIRE_RETRY_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return keys.stream().filter(k -> !leftKeys.contains(k)).collect(Collectors.toList());
            }
        }
    }
}

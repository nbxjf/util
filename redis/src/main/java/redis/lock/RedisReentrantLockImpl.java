package redis.lock;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.common.RedisKeyType;
import redis.pool.RedisPool;

/**
 * Created by Jeff_xu on 2021/9/17.
 *
 * @author Jeff_xu
 */
@Slf4j
class RedisReentrantLockImpl<K> extends AbstractRedisLock<K> implements RedisLock {

    private final List<K> keys;
    private final AtomicLong lockedCounter;
    public static final int ACQUIRE_RETRY_INTERVAL = 100;

    public RedisReentrantLockImpl(RedisPool redisPool,
                                  RedisKeyType<K> keyType,
                                  List<K> keys,
                                  long expiryTimeMillis,
                                  String scope) {
        super(redisPool, keyType, expiryTimeMillis, scope);
        this.keys = keys;
        this.lockedCounter = new AtomicLong();
    }

    @Override
    public void acquire() throws InterruptedException {
        while (!tryAcquireOnce(redisPool.getJedisClient())) {
            //noinspection BusyWait
            Thread.sleep(ACQUIRE_RETRY_INTERVAL);
        }
    }

    @Override
    public boolean tryAcquire() {
        return tryAcquireOnce(redisPool.getJedisClient());
    }

    @Override
    public boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
        if (timeout <= 0) {
            return tryAcquireOnce(redisPool.getJedisClient());
        }
        long deadline = System.currentTimeMillis() + unit.toMillis(timeout);
        final Jedis jedisClient = redisPool.getJedisClient();
        for (; ; ) {
            if (tryAcquireOnce(jedisClient)) {
                return true;
            }
            if (System.currentTimeMillis() + ACQUIRE_RETRY_INTERVAL > deadline) {
                return false;
            }
            Thread.sleep(ACQUIRE_RETRY_INTERVAL);
        }
    }

    @Override
    public boolean release() throws IllegalStateException {
        if (lockedCounter.getAndUpdate(c -> c > 0 ? c - 1 : c) > 0) {
            List<K> releasedKeys = doRelease(redisPool.getJedisClient(), keys);
            return releasedKeys.size() == keys.size();
        } else {
            throw new IllegalStateException("not acquired yet");
        }
    }

    private boolean tryAcquireOnce(Jedis jedis) {
        List<K> lockedKeys = doAcquire(jedis, keys);

        // 判断是否获取了所有的锁
        // 已获取所有锁，直接返回
        if (lockedKeys.size() == keys.size()) {
            lockedCounter.incrementAndGet();
            return true;
        }

        if (!lockedKeys.isEmpty()) {
            log.warn("failed to acquire redis lock with multi keys, " +
                "start to delete the acquired ones {}", lockedKeys);
            doRelease(jedis, lockedKeys);
        }
        return false;
    }
}

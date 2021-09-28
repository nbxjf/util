package redis.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;
import redis.common.RedisKeyType;
import redis.lock.RedisLockService;
import redis.pool.RedisPool;

/**
 * Created by Jeff_xu on 2021/9/17.
 * 基于redis的缓存服务封装
 *
 * @author Jeff_xu
 */
@Slf4j
public class RedisCacheService {

    private final RedisPool redisPool;
    private final RedisLockService redisLockService;
    /**
     * 用于异步刷新缓存的线程池
     */
    private final ExecutorService asyncLoadExecutor;

    public RedisCacheService(RedisPool redisPool) {
        this.redisPool = redisPool;
        this.redisLockService = new RedisLockService(redisPool);

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

    public <K, V> V cache(String prefix,
                          K key,
                          int physicalExpireSeconds,
                          boolean cacheNullable,
                          Supplier<V> cacheLoader) {
        return null;
    }

    public <K, V> V cache(String prefix,
                          K key,
                          int logicalExpireSeconds,
                          int physicalExpireSeconds,
                          boolean cacheNullable,
                          Supplier<V> cacheLoader) {
        return null;
    }

    public <K, V> V cache(String prefix,
                          K key,
                          int logicalExpireSeconds,
                          int physicalExpireSeconds,
                          boolean cacheNullable,
                          RedisKeyType<K> keyType,
                          Supplier<V> cacheLoader) {
        return null;
    }

    public <K, V> Map<K, V> bulkCache(String keyPrefix,
                                      String version,
                                      List<K> keys,
                                      int logicalExpireSeconds,
                                      int physicalExpireSeconds,
                                      boolean cacheNullable,
                                      RedisCacheLoader<K, V> valueLoader) {
        return null;
    }

    public <K, V> Map<K, V> bulkCache(String keyPrefix,
                                      String version,
                                      List<K> keys,
                                      int logicalExpireSeconds,
                                      int physicalExpireSeconds,
                                      boolean cacheNullable,
                                      RedisKeyType<K> keyType,
                                      RedisCacheLoader<K, V> valueLoader) {
        return null;
    }
}

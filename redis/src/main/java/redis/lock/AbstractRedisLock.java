package redis.lock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import redis.clients.jedis.Jedis;
import redis.common.AbstractRedisSupport;
import redis.common.RedisKeyType;
import redis.common.RedisValueType;
import redis.pool.RedisPool;

/**
 * Created by Jeff_xu on 2021/9/22.
 *
 * @author Jeff_xu
 */
public class AbstractRedisLock<K> extends AbstractRedisSupport<K, String> {

    private static final String LOCK_SCRIPT = "" +
        // 存在key，但是该field的值不为0，则认为此时是别的线程在占用锁
        "if (redis.call('exists', KEYS[1]) > 0 and redis.call('hexists', KEYS[1], ARGV[1]) == 0) then " +
        "return nil;" +
        "end " +
        "redis.call('hincrby', KEYS[1], ARGV[1], 1); " +
        "redis.call('pexpire', KEYS[1], ARGV[2]); " +
        "return 'OK';" +
        "";

    private static final String RELEASE_SCRIPT = "" +
        "if (redis.call('hexists', KEYS[1], ARGV[1]) > 0) then " +
        "if (redis.call('hincrby', KEYS[1], ARGV[1], -1) < 1) then " +
        "redis.call('del', KEYS[1]); " +
        "end " +
        "return 'OK';" +
        "end" +
        "";

    private static final String USE_SPACE = "lock";

    protected final RedisKeyType<K> keyType;
    protected final long expiryTimeMillis;
    protected final String serviceUniqKey;

    public AbstractRedisLock(RedisPool redisPool,
                             RedisKeyType<K> keyType,
                             long expiryTimeMillis,
                             String scope) {
        super(USE_SPACE + "/" + scope, keyType, RedisValueType.STRING, redisPool);
        this.keyType = keyType;
        this.serviceUniqKey = UUID.randomUUID().toString();
        this.expiryTimeMillis = expiryTimeMillis;
    }

    /**
     * 申请一批分布式锁，返回成功的keys
     *
     * @param jedis      jedis实例
     * @param toLockKeys 需要加锁的key
     * @return 加锁成功的keys
     */
    protected List<K> doAcquire(Jedis jedis, List<K> toLockKeys) {
        List<Object> pipelineResponse = pipelineEval(jedis, LOCK_SCRIPT, toLockKeys, Arrays.asList(threadKey(), String.valueOf(expiryTimeMillis)));
        return filterLockedKeys(toLockKeys, pipelineResponse);
    }

    /**
     * 释放一批分布式锁，返回释放成功的keys
     *
     * @param jedis         jedis实例
     * @param toReleaseKeys 要释放的keys
     * @return 释放成功的redis keys
     */
    protected List<K> doRelease(Jedis jedis, List<K> toReleaseKeys) {
        List<Object> pipelineResponse = pipelineEval(jedis, RELEASE_SCRIPT, toReleaseKeys, Arrays.asList(threadKey(), String.valueOf(expiryTimeMillis)));
        return filterLockedKeys(toReleaseKeys, pipelineResponse);
    }

    /**
     * field构建
     */
    protected String threadKey() {
        return serviceUniqKey + "_" + Thread.currentThread().getId();
    }

    protected List<K> filterLockedKeys(List<K> keys, List<Object> responses) {
        List<K> lockedKeys = new ArrayList<>(keys.size());
        for (int i = 0; i < keys.size(); i++) {
            if (responseIsOk((String)responses.get(i))) {
                lockedKeys.add(keys.get(i));
            }
        }
        return lockedKeys;
    }

    protected boolean responseIsOk(String response) {
        return "OK".equalsIgnoreCase(response);
    }
}

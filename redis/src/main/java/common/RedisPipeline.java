package common;

import pool.RedisPool;

public class RedisPipeline<K, V> extends AbstractRedisSupport<K, V> {

    public RedisPipeline(RedisPool redisPool,
                         String keySpace,
                         RedisKeyType<K> keyType,
                         RedisValueType<V> valueType) {
        super(keySpace, keyType, valueType, redisPool);
    }
}

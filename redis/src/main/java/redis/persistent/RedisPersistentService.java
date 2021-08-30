package redis.persistent;

import redis.common.DefaultRedisKeyType;
import redis.common.DefaultRedisValueType;
import redis.common.RedisKeyType;
import redis.common.RedisMap;
import redis.common.RedisValueType;
import redis.pool.RedisPool;
import utils.serializer.FastJsonSerializer;

public class RedisPersistentService<K, V> {

    private final RedisPool redisPool;
    private final String prefixSpace;

    /**
     * 构造一个redis表服务
     *
     * @param pool 构造一个redis表服务需要的redis连接池
     */
    protected RedisPersistentService(RedisPool pool) {
        this(pool, "persistent");
    }

    protected RedisPersistentService(RedisPool pool, String prefixSpace) {
        this.redisPool = pool;
        this.prefixSpace = prefixSpace;
    }

    /**
     * 获取指定的redis map，redis map是访问redis最基本数据结构（key-value）的方式
     *
     * @param stringKey 需要访问的redis map的名称
     * @param valueType map的value类型
     * @return 对应的redis map实例
     */
    public RedisMap<K, V> string(String stringKey, Class<V> valueType) {
        return this.map(stringKey, valueType);
    }

    /**
     * 获取指定的redis map，redis map是访问redis最基本数据结构（key-value）的方式
     *
     * @param key       需要访问的redis map的名称，以对象作为mapKey
     * @param valueType map的value类型
     * @return 对应的redis map实例
     */
    public RedisMap<K, V> string(K key, Class<V> valueType) {
        return this.map(key, valueType);
    }

    /**
     * 获取指定的redis map，redis map是访问redis最基本数据结构（key-value）的方式
     *
     * @param mapName   需要访问的redis map的名称
     * @param valueType map的value类型
     * @return 对应的redis map实例
     */
    public RedisMap<K, V> map(String mapName, Class<V> valueType) {
        return this.map(mapName, RedisKeyType.normal(), new DefaultRedisValueType<>(valueType, new FastJsonSerializer()));
    }

    /**
     * 获取指定的redis map，redis map是访问redis最基本数据结构（key-value）的方式
     *
     * @param mapKey    需要访问的redis map的名称，以对象作为mapKey
     * @param valueType map的value类型
     * @return 对应的redis map实例
     */
    @SuppressWarnings("unchecked")
    public RedisMap<K, V> map(K mapKey, Class<V> valueType) {
        final DefaultRedisKeyType<K> vDefaultRedisKeyType = new DefaultRedisKeyType<>((Class<K>)mapKey.getClass());
        return this.map(vDefaultRedisKeyType.toString(mapKey), vDefaultRedisKeyType, new DefaultRedisValueType<>(valueType, new FastJsonSerializer()));
    }

    /**
     * 获取指定的redis map，redis map是访问redis最基本数据结构（key-value）的方式
     *
     * @param mapName        需要访问的redis map的名称
     * @param redisKeyType   key的数据类型
     * @param redisValueType value的类型
     * @return 对应的redis map实例
     */
    public RedisMap<K, V> map(String mapName, RedisKeyType<K> redisKeyType, RedisValueType<V> redisValueType) {
        return new RedisMap<>(redisPool, getRealKeySpace(mapName), redisKeyType, redisValueType);
    }

    private String getRealKeySpace(String suffixSpace) {
        if (suffixSpace == null || suffixSpace.isEmpty()) {
            throw new IllegalArgumentException("entity name must not be empty");
        }
        return prefixSpace + "/" + suffixSpace;
    }

}

package redis.persistent;

import com.sun.istack.internal.Nullable;
import redis.common.RedisFieldType;
import redis.common.RedisFieldInstance;
import redis.common.RedisValueInstance;
import redis.common.RedisHash;
import redis.common.RedisKeyType;
import redis.common.RedisList;
import redis.common.RedisMap;
import redis.common.RedisSet;
import redis.common.RedisString;
import redis.common.RedisValueType;
import redis.pool.RedisPool;
import utils.serializer.FastJsonSerializer;
import utils.serializer.Serializer;

public class RedisPersistentService {

    private final RedisPool redisPool;
    private final String useSpace;
    private final Serializer serializer;

    /**
     * 构造一个redis表服务
     *
     * @param pool 构造一个redis表服务需要的redis连接池
     */
    public RedisPersistentService(RedisPool pool) {
        this(pool, "persistent");
    }

    protected RedisPersistentService(RedisPool pool, String useSpace) {
        this(pool, useSpace, new FastJsonSerializer());
    }

    protected RedisPersistentService(RedisPool pool, String useSpace, Serializer serializer) {
        this.redisPool = pool;
        this.useSpace = useSpace;
        this.serializer = serializer;
    }

    /**
     * 获取指定的redis string的对象
     *
     * @param valueType string的value类型
     * @return 对应的redis map实例
     */
    public <V> RedisString<V> string(Class<V> valueType) {
        return new RedisString<>(redisPool, new RedisValueInstance<>(valueType, serializer));
    }

    /**
     * 获取指定的redis map，redis map是访问redis最基本数据结构（key-value）的方式
     *
     * @param mapName   需要访问的redis map的名称
     * @param valueType map的value类型
     * @return 对应的redis map实例
     */
    public <V> RedisMap<String, V> map(String mapName, Class<V> valueType) {
        return this.map(mapName, RedisKeyType.normal(), new RedisValueInstance<>(valueType, serializer));
    }

    /**
     * 获取指定的redis list
     *
     * @param listName  list的string名称
     * @param valueType list的成员类型
     * @param <V>       list的成员类型
     * @return redis list 实例
     */
    public <V> RedisList<String, V> list(String listName, Class<V> valueType) {
        return this.list("", listName, new RedisValueInstance<>(valueType, serializer));
    }

    /**
     * 获取指定的redis list，以对象作为key
     *
     * @param listPrefix list的string名称的前缀
     * @param key        list的名称对象，完成的key = listPrefix + key.string
     * @param valueType  list的成员类型
     * @param <V>        list的成员类型
     * @return redis list 实例
     */
    public <K, V> RedisList<K, V> list(@Nullable String listPrefix, K key, Class<V> valueType) {
        return this.list(listPrefix, key, new RedisValueInstance<>(valueType, serializer));
    }

    /**
     * 获取指定的redis list
     *
     * @param listName  list的string名称
     * @param valueType list的成员类型
     * @param <V>       list的成员类型
     * @return redis list 实例
     */
    public <V> RedisSet<String, V> set(String listName, Class<V> valueType) {
        return this.set("", listName, new RedisValueInstance<>(valueType, serializer));
    }

    /**
     * 获取指定的redis list，以对象作为key
     *
     * @param listPrefix list的string名称的前缀
     * @param key        list的名称对象，完成的key = listPrefix + key.string
     * @param valueType  list的成员类型
     * @param <V>        list的成员类型
     * @return redis list 实例
     */
    public <K, V> RedisSet<K, V> set(@Nullable String listPrefix, K key, Class<V> valueType) {
        return this.set(listPrefix, key, new RedisValueInstance<>(valueType, serializer));
    }

    /**
     * 获取指定的redis hash，key和field都是string
     *
     * @param hashName  hash的名称
     * @param valueType value对象的类型
     * @param <V>       value对象的类型
     * @return redis hash 实例
     */
    public <V> RedisHash<String, String, V> hash(String hashName, Class<V> valueType) {
        return this.hash(hashName, String.class, valueType);
    }

    /**
     * 获取指定的redis hash，key是string,value是特定类型
     *
     * @param hashName  hash的名称
     * @param fieldType field的对象类型
     * @param valueType value对象的类型
     * @param <F>       field的对象类型
     * @param <V>       value对象的类型
     * @return redis hash 实例
     */
    public <F, V> RedisHash<String, F, V> hash(String hashName, Class<F> fieldType, Class<V> valueType) {
        return this.hash("", hashName, new RedisFieldInstance<>(fieldType, serializer), new RedisValueInstance<>(valueType, serializer));
    }

    /**
     * 获取指定的redis hash，key是string,value是特定类型
     *
     * @param hashPrefix hash的名称的前缀，可为空
     * @param fieldType  field的对象类型
     * @param valueType  value对象的类型
     * @param <F>        field的对象类型
     * @param <V>        value对象的类型
     * @return redis hash 实例
     */
    public <K, F, V> RedisHash<K, F, V> hash(@Nullable String hashPrefix, K key, Class<F> fieldType, Class<V> valueType) {
        return this.hash(hashPrefix, key, new RedisFieldInstance<>(fieldType, serializer), new RedisValueInstance<>(valueType, serializer));
    }

    /**
     * 获取指定的redis map，redis map是访问redis最基本数据结构（key-value）的方式
     *
     * @param mapName        需要访问的redis map的名称
     * @param redisKeyType   key的数据类型
     * @param redisValueType value的类型
     * @return 对应的redis map实例
     */
    protected <K, V> RedisMap<K, V> map(String mapName, RedisKeyType<K> redisKeyType, RedisValueType<V> redisValueType) {
        return new RedisMap<>(redisPool, getRealKeyPrefix(mapName), redisKeyType, redisValueType);
    }

    protected <K, V> RedisList<K, V> list(String listPrefix, K key, RedisValueType<V> redisValueType) {
        return new RedisList<>(redisPool, listPrefix, key, redisValueType);
    }

    protected <K, V> RedisSet<K, V> set(String listPrefix, K key, RedisValueType<V> redisValueType) {
        return new RedisSet<>(redisPool, listPrefix, key, redisValueType);
    }

    protected <K, F, V> RedisHash<K, F, V> hash(String hashPrefix, K key, RedisFieldType<F> fieldType, RedisValueType<V> redisValueType) {
        return new RedisHash<>(redisPool, hashPrefix, key, fieldType, redisValueType);
    }

    private String getRealKeyPrefix(String suffixSpace) {
        if (suffixSpace == null || suffixSpace.isEmpty()) {
            throw new IllegalArgumentException("entity name must not be empty");
        }
        return useSpace + "/" + suffixSpace;
    }

}

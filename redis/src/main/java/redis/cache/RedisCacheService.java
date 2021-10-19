package redis.cache;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;
import redis.common.RedisKeyType;
import redis.pool.RedisPool;
import utils.serializer.Serializer;

/**
 * Created by Jeff_xu on 2021/9/17.
 * 基于redis的缓存服务封装
 *
 * @author Jeff_xu
 */
@Slf4j
public class RedisCacheService {

    private final RedisPool redisPool;
    private final Serializer serializer;

    public RedisCacheService(RedisPool redisPool, Serializer serializer) {
        this.redisPool = redisPool;
        this.serializer = serializer;
    }

    /**
     * 根据key获取其对应的缓存，如果不存在或已过期则对其进行求值并存入缓存
     *
     * @param prefix                缓存key的前缀，表示缓存的业务范围，不要已/开头和结束
     * @param version               缓存的版本号，如果缓存需要不兼容升级，可以直接通过版本号进行缓存隔离
     * @param key                   要获取的缓存的key
     * @param physicalExpireSeconds 缓存的物理过期时间，缓存在达到此时间后会被删除
     * @param cacheNullable         缓存时候可以为null
     * @param cacheLoader           缓存击穿时的函数回调
     * @param <K>                   缓存中的key的类型
     * @param <V>                   缓存中的值的类型
     * @return cache obj
     */
    public <K, V> V cache(String prefix,
                          String version,
                          K key,
                          int physicalExpireSeconds,
                          boolean cacheNullable,
                          Supplier<V> cacheLoader) {
        return cache(prefix, version, key, physicalExpireSeconds, physicalExpireSeconds, cacheNullable, cacheLoader);
    }

    /**
     * 根据key获取其对应的缓存，如果不存在或已过期则对其进行求值并存入缓存
     *
     * @param prefix                缓存key的前缀，表示缓存的业务范围，不要已/开头和结束
     * @param version               缓存的版本号，如果缓存需要不兼容升级，可以直接通过版本号进行缓存隔离
     * @param key                   要获取的缓存的key
     * @param logicalExpireSeconds  缓存的逻辑过期时间。如果被请求的缓存已逻辑过期但未物理过期，则返回该缓存数据并触发异步刷新
     * @param physicalExpireSeconds 缓存的物理过期时间，缓存在达到此时间后会被删除
     * @param cacheNullable         缓存时候可以为null
     * @param cacheLoader           缓存击穿时的函数回调
     * @param <K>                   缓存中的key的类型
     * @param <V>                   缓存中的值的类型
     * @return cache obj
     */
    public <K, V> V cache(String prefix,
                          String version,
                          K key,
                          int logicalExpireSeconds,
                          int physicalExpireSeconds,
                          boolean cacheNullable,
                          Supplier<V> cacheLoader) {
        return cache(prefix, version, key, logicalExpireSeconds, physicalExpireSeconds, cacheNullable, RedisKeyType.normal(), cacheLoader);
    }

    /**
     * 根据key获取其对应的缓存，如果不存在或已过期则对其进行求值并存入缓存
     *
     * @param prefix                缓存key的前缀，表示缓存的业务范围，不要已/开头和结束
     * @param version               缓存的版本号，如果缓存需要不兼容升级，可以直接通过版本号进行缓存隔离
     * @param key                   要获取的缓存的key
     * @param logicalExpireSeconds  缓存的逻辑过期时间。如果被请求的缓存已逻辑过期但未物理过期，则返回该缓存数据并触发异步刷新
     * @param physicalExpireSeconds 缓存的物理过期时间，缓存在达到此时间后会被删除
     * @param cacheNullable         缓存时候可以为null
     * @param keyType               key类型描述
     * @param cacheLoader           缓存击穿时的函数回调
     * @param <K>                   缓存中的key的类型
     * @param <V>                   缓存中的值的类型
     * @return cache obj
     */
    public <K, V> V cache(String prefix,
                          String version,
                          K key,
                          int logicalExpireSeconds,
                          int physicalExpireSeconds,
                          boolean cacheNullable,
                          RedisKeyType<K> keyType,
                          Supplier<V> cacheLoader) {
        return new RedisCacheOperatorImpl<K, V>(redisPool, prefix, version, logicalExpireSeconds, physicalExpireSeconds, cacheNullable, keyType, serializer)
            .load(key, k -> cacheLoader.get());
    }

    /**
     * 批量获取缓存，如果不存在或已过期则对其进行求值并存入缓存
     *
     * @param keyPrefix             缓存key的前缀，表示缓存的业务范围，不要已/开头和结束
     * @param version               缓存的版本号，如果缓存需要不兼容升级，可以直接通过版本号进行缓存隔离
     * @param keys                  要获取的缓存的keys
     * @param logicalExpireSeconds  缓存的逻辑过期时间。如果被请求的缓存已逻辑过期但未物理过期，则返回该缓存数据并触发异步刷新
     * @param physicalExpireSeconds 缓存的物理过期时间，缓存在达到此时间后会被删除
     * @param cacheNullable         缓存时候可以为null
     * @param valueLoader           缓存击穿时的函数回调
     * @param <K>                   缓存中的key的类型
     * @param <V>                   缓存中的值的类型
     * @return redis中的缓存
     */
    public <K, V> Map<K, V> bulkCache(String keyPrefix,
                                      String version,
                                      List<K> keys,
                                      int logicalExpireSeconds,
                                      int physicalExpireSeconds,
                                      boolean cacheNullable,
                                      RedisCacheLoader<K, V> valueLoader) {
        return bulkCache(keyPrefix, version, keys, logicalExpireSeconds, physicalExpireSeconds, cacheNullable, RedisKeyType.normal(), valueLoader);
    }

    /**
     * 批量获取缓存，如果不存在或已过期则对其进行求值并存入缓存
     *
     * @param keyPrefix             缓存key的前缀，表示缓存的业务范围，不要已/开头和结束
     * @param version               缓存的版本号，如果缓存需要不兼容升级，可以直接通过版本号进行缓存隔离
     * @param keys                  要获取的缓存的keys
     * @param logicalExpireSeconds  缓存的逻辑过期时间。如果被请求的缓存已逻辑过期但未物理过期，则返回该缓存数据并触发异步刷新
     * @param physicalExpireSeconds 缓存的物理过期时间，缓存在达到此时间后会被删除
     * @param cacheNullable         缓存时候可以为null
     * @param keyType               key类型描述
     * @param valueLoader           缓存击穿时的函数回调
     * @param <K>                   缓存中的key的类型
     * @param <V>                   缓存中的值的类型
     * @return redis中的缓存
     */
    public <K, V> Map<K, V> bulkCache(String keyPrefix,
                                      String version,
                                      List<K> keys,
                                      int logicalExpireSeconds,
                                      int physicalExpireSeconds,
                                      boolean cacheNullable,
                                      RedisKeyType<K> keyType,
                                      RedisCacheLoader<K, V> valueLoader) {
        return new RedisCacheOperatorImpl<K, V>(redisPool, keyPrefix, version, logicalExpireSeconds, physicalExpireSeconds, cacheNullable, keyType, serializer)
            .load(keys, valueLoader);
    }

    /**
     * 构建一个redis缓存的操作器实例，通过该操作器可以完成对于缓存的操作
     *
     * @param keyPrefix             缓存key的前缀，表示缓存的业务范围，不要已/开头和结束
     * @param version               缓存的版本号，如果缓存需要不兼容升级，可以直接通过版本号进行缓存隔离
     * @param logicalExpireSeconds  缓存的逻辑过期时间。如果被请求的缓存已逻辑过期但未物理过期，则返回该缓存数据并触发异步刷新
     * @param physicalExpireSeconds 缓存的物理过期时间，缓存在达到此时间后会被删除
     * @param cacheNullable         缓存时候可以为null
     * @param <K>                   缓存中的key的类型
     * @param <V>                   缓存中的值的类型
     * @return redis缓存操作器
     */
    public <K, V> RedisCacheOperator<K, V> operator(String keyPrefix,
                                                    String version,
                                                    int logicalExpireSeconds,
                                                    int physicalExpireSeconds,
                                                    boolean cacheNullable) {
        return operator(keyPrefix, version, logicalExpireSeconds, physicalExpireSeconds, cacheNullable, RedisKeyType.normal());
    }

    /**
     * 构建一个redis缓存的操作器实例，通过该操作器可以完成对于缓存的操作
     *
     * @param keyPrefix             缓存key的前缀，表示缓存的业务范围，不要已/开头和结束
     * @param version               缓存的版本号，如果缓存需要不兼容升级，可以直接通过版本号进行缓存隔离
     * @param logicalExpireSeconds  缓存的逻辑过期时间。如果被请求的缓存已逻辑过期但未物理过期，则返回该缓存数据并触发异步刷新
     * @param physicalExpireSeconds 缓存的物理过期时间，缓存在达到此时间后会被删除
     * @param cacheNullable         缓存时候可以为null
     * @param redisKeyType          key类型描述
     * @param <K>                   缓存中的key的类型
     * @param <V>                   缓存中的值的类型
     * @return redis缓存操作器
     */
    public <K, V> RedisCacheOperator<K, V> operator(String keyPrefix,
                                                    String version,
                                                    int logicalExpireSeconds,
                                                    int physicalExpireSeconds,
                                                    boolean cacheNullable,
                                                    RedisKeyType<K> redisKeyType) {
        return new RedisCacheOperatorImpl<K, V>(redisPool, keyPrefix, version, logicalExpireSeconds, physicalExpireSeconds, cacheNullable, redisKeyType, serializer);
    }
}

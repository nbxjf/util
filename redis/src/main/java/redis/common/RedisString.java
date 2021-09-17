package redis.common;

import java.util.List;
import java.util.Map;

import redis.pool.RedisPool;

/**
 * Created by Jeff_xu on 2021/9/16.
 *
 * @author Jeff_xu
 */
public class RedisString<V> {

    private final RedisMap<String, V> redisMap;

    public RedisString(RedisPool redisPool,
                       RedisValueType<V> valueType) {
        this.redisMap = new RedisMap<>(redisPool, "", RedisKeyType.STRING, valueType);
    }

    /**
     * 获取表中指定的key对应的值
     *
     * @param key 要获取的key
     * @return 返回key对应的值（或null如果不存在）
     */
    public V get(String key) {
        return redisMap.get(key);
    }

    /**
     * 获取表中一组key对应的值
     *
     * @param keys 要获取的key列表
     * @return 返回key对应的值（或null如果不存在）
     */
    public Map<String, V> get(List<String> keys) {
        return redisMap.get(keys);
    }

    /**
     * 将指定的key设置为给定的值
     *
     * @param key   要设置的key
     * @param value 要设置的值
     */
    public void set(String key, V value) {
        redisMap.set(key, value);
    }

    /**
     * 将指定的key设置为给定的值，并设置有效期
     *
     * @param key   要设置的key
     * @param value 要设置的值
     * @param ttl   value的有效期，单位毫秒
     */
    public void set(String key, V value, long ttl) {
        redisMap.set(key, value, ttl);
    }

    /**
     * 设置一组指定的key和对应的value
     *
     * @param keyValues 要设置的key和其对应的value
     */
    public void set(Map<String, V> keyValues) {
        redisMap.set(keyValues);
    }

    /**
     * 设置一组指定的key和对应的value，并设置有效期
     *
     * @param keyValues 要设置的key和其对应的value
     * @param ttl       value的有效期，单位毫秒
     */
    public void set(Map<String, V> keyValues, long ttl) {
        redisMap.set(keyValues, ttl);
    }

    /**
     * 删除指定的key
     *
     * @param key 要删除的key
     * @return 返回是否删除成功
     */
    public boolean del(String key) {
        return redisMap.del(key);
    }

    /**
     * 删除指定的一组key
     *
     * @param keys 要删除的key列表
     * @return 返回是否删除成功
     */
    public int del(List<String> keys) {
        return redisMap.del(keys);
    }

    /**
     * 自增指定的key，并返回增加后的值
     *
     * @param key 要自增的key
     * @return 返回自增之后的值
     */
    public long incr(String key) {
        return redisMap.incr(key);
    }

    /**
     * 自增指定的key，并返回增加后的值
     *
     * @param key 要自增的key
     * @param ttl key应在多少毫秒之后过期
     * @return 返回自增之后的值
     */
    public long incr(String key, long ttl) {
        return redisMap.incr(key, ttl);
    }

    /**
     * 将指定的key的值自增delta，并返回增加后的值
     *
     * @param key   要自增的key
     * @param delta 自增的幅度
     * @return 返回自增之后的值
     */
    public long incrBy(String key, long delta) {
        return redisMap.incrBy(key, delta);
    }

    /**
     * 将指定的key的值自增delta，并返回增加后的值
     *
     * @param key   要自增的key
     * @param delta 自增的幅度
     * @param ttl   key应在多少毫秒之后过期
     * @return 返回自增之后的值
     */
    public long incrBy(String key, long delta, long ttl) {
        return redisMap.incrBy(key, delta, ttl);
    }
}




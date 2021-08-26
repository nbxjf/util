package common;

import pool.RedisConfig;
import pool.RedisPool;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedisMap<K, V> extends AbstractRedisSupport<K, V> {

    public RedisMap(RedisPool redisPool,
                    String keySpace,
                    RedisKeyType<K> keyType,
                    RedisValueType<V> valueType) {
        super(keySpace, keyType, valueType, redisPool);
    }

    /**
     * 获取表中指定的key对应的值
     *
     * @param key 要获取的key
     * @return 返回key对应的值（或null如果不存在）
     */
    public V get(K key) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return returnValue(jedis.get(finalKey(key)));
        }
    }

    /**
     * 获取表中一组key对应的值
     *
     * @param keys 要获取的key列表
     * @return 返回key对应的值（或null如果不存在）
     */
    public Map<K, V> get(List<K> keys) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            List<V> values = returnValues(jedis.mget(finalKeys(keys)));
            Map<K, V> map = new HashMap<>(keys.size());
            for (int i = 0; i < keys.size(); i++) {
                V value = values.get(i);
                if (value != null) {
                    map.put(keys.get(i), value);
                }
            }
            return map;
        }
    }

    /**
     * 将指定的key设置为给定的值
     *
     * @param key   要设置的key
     * @param value 要设置的值
     */
    public String set(K key, V value) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.set(finalKey(key), finalValue(value));
        }
    }

    /**
     * 将指定的key设置为给定的值，并设置有效期
     *
     * @param key   要设置的key
     * @param value 要设置的值
     * @param ttl   value的有效期，单位毫秒
     */
    public void set(K key, V value, long ttl) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            Pipeline pipeline = jedis.pipelined();
            pipeline.set(finalKey(key), finalValue(value));
            pipeline.pexpire(finalKey(key), ttl);
            List<Object> objects = pipeline.syncAndReturnAll();
            for (Object o : objects) {
                if (o instanceof JedisDataException) {
                    throw (JedisDataException) o;
                }
            }
        }
    }


    /**
     * 设置一组指定的key和对应的value
     *
     * @param keyValues 要设置的key和其对应的value
     */
    public void set(Map<K, V> keyValues) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            Pipeline pipeline = jedis.pipelined();
            for (Map.Entry<K, V> entry : keyValues.entrySet()) {
                pipeline.set(finalKey(entry.getKey()), finalValue(entry.getValue()));
            }
            syncAndCheck(pipeline);
        }
    }

    /**
     * 设置一组指定的key和对应的value，并设置有效期
     *
     * @param keyValues 要设置的key和其对应的value
     * @param ttl       value的有效期，单位毫秒
     */
    public void set(Map<K, V> keyValues, long ttl) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            Pipeline pipeline = jedis.pipelined();
            for (Map.Entry<K, V> entry : keyValues.entrySet()) {
                pipeline.set(finalKey(entry.getKey()), finalValue(entry.getValue()));
                pipeline.pexpire(finalKey(entry.getKey()), ttl);
            }
            syncAndCheck(pipeline);
        }
    }

    /**
     * 删除指定的key
     *
     * @param key 要删除的key
     * @return 返回是否删除成功
     */
    public boolean del(K key) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.del(finalKey(key)) > 0;
        }
    }

    /**
     * 删除指定的一组key
     *
     * @param keys 要删除的key列表
     * @return 返回是否删除成功
     */
    public int del(List<K> keys) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.del(finalKeys(keys)).intValue();
        }
    }

    /**
     * 自增指定的key，并返回增加后的值
     *
     * @param key 要自增的key
     * @return 返回自增之后的值
     */
    public long incr(K key) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.incr(finalKey(key));
        }
    }

    /**
     * 自增指定的key，并返回增加后的值
     *
     * @param key 要自增的key
     * @param ttl key应在多少毫秒之后过期
     * @return 返回自增之后的值
     */
    public long incr(K key, long ttl) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            Pipeline pipeline = jedis.pipelined();
            Response<Long> response = pipeline.incr(finalKey(key));
            pipeline.pexpire(finalKey(key), ttl);
            syncAndCheck(pipeline);
            return response.get();
        }
    }

    /**
     * 将指定的key的值自增delta，并返回增加后的值
     *
     * @param key   要自增的key
     * @param delta 自增的幅度
     * @return 返回自增之后的值
     */
    public long incrBy(K key, long delta) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.incrBy(finalKey(key), delta);
        }
    }

    /**
     * 将指定的key的值自增delta，并返回增加后的值
     *
     * @param key   要自增的key
     * @param delta 自增的幅度
     * @param ttl   key应在多少毫秒之后过期
     * @return 返回自增之后的值
     */
    public long incrBy(K key, long delta, long ttl) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            Pipeline pipeline = jedis.pipelined();
            Response<Long> response = pipeline.incrBy(finalKey(key), delta);
            pipeline.pexpire(finalKey(key), ttl);
            syncAndCheck(pipeline);
            return response.get();
        }
    }


    /**
     * 同步所有操作，然后对操作结果进行检查，并抛出第一个遇到的redis异常（如果存在）
     */
    public void syncAndCheck(Pipeline pipeline) {
        for (Object o : pipeline.syncAndReturnAll()) {
            if (o instanceof JedisDataException) {
                throw (JedisDataException) o;
            }
        }
    }

    public static void main(String[] args) {
        RedisConfig redisConfig = new RedisConfig();
        redisConfig.setHost("127.0.0.1");
        redisConfig.setDatabase(4);
        redisConfig.setPassword("54xujifa");
        RedisPool redisPool = new RedisPool(redisConfig);

        RedisMap<String, String> map = new RedisMap<>(redisPool, "test", RedisKeyType.normal(), new FastJsonSerializableValueType<>(String.class));
        map.set("xujifa", "1995");

        String xujifa = map.get("xujifa");
        System.out.println(xujifa);
    }
}

package redis.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import lombok.val;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.pool.RedisPool;

@SuppressWarnings("unchecked")
public class RedisHash<K, F, V> extends AbstractRedisSupport<K, V> {

    private final byte[] hashName;
    private final RedisFieldType<F> fieldType;

    public RedisHash(K redisKey,
                     RedisPool redisPool,
                     String keySpace,
                     RedisFieldType<F> fieldType,
                     RedisValueType<V> valueType) {
        super(keySpace, new DefaultRedisKeyType<>((Class<K>)redisKey.getClass()), valueType, redisPool);
        this.hashName = finalKey(redisKey);
        this.fieldType = fieldType;
    }

    public V get(F field) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return returnValue(jedis.hget(hashName, toBytes(fieldType.toString(field))));
        }
    }

    public V getOrDefault(F field, V defaultValue) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            V value = returnValue(jedis.hget(hashName, toBytes(fieldType.toString(field))));
            return value != null ? value : defaultValue;
        }
    }

    public Map<F, V> gets(List<F> fields) {
        if (fields.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<F, V> map = new HashMap<>(fields.size());
        try (Jedis jedis = redisPool.getJedisClient()) {
            List<V> values = returnValues(jedis.hmget(hashName, finalFields(fields)));
            for (int i = 0; i < fields.size(); i++) {
                V value = values.get(i);
                if (value != null) {
                    map.put(fields.get(i), value);
                }
            }
        }
        return map;
    }

    public void scan(int batchSize, Consumer<Entry<F, V>> visitor) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            String cursor = "0";
            ScanParams params = new ScanParams().count(batchSize);
            for (; ; ) {
                ScanResult<Entry<F, V>> sr = returnEntryScanResult(jedis.hscan(hashName, toBytes(cursor), params));
                if ("0".equals(sr.getCursor())) {
                    break;
                }
                sr.getResult().forEach(visitor);
                cursor = sr.getCursor();
            }
        }
    }

    public void set(F field, V value) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            jedis.hset(hashName, finalField(field), finalValue(value));
        }
    }

    /**
     * Set the specified hash field to the specified value if the field not exists. <b>Time
     * complexity:</b> O(1)
     *
     * @return If the field already exists, 0 is returned, otherwise if a new field is created 1 is
     * returned.
     */
    public Long setnx(F field, V value) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.hsetnx(hashName, finalField(field), finalValue(value));
        }
    }

    public void sets(Map<F, V> valueMap) {
        if (valueMap.isEmpty()) {
            return;
        }
        try (Jedis jedis = redisPool.getJedisClient()) {
            jedis.hmset(hashName, finalValueMap(valueMap));
        }
    }

    public Long incr(F field) {
        return incrBy(field, 1);
    }

    /**
     * Increment the number stored at field in the hash at key by value. If key does not exist, a new
     * key holding a hash is created. If field does not exist or holds a string, the value is set to 0
     * before applying the operation. Since the value argument is signed you can use this command to
     * perform both increments and decrements.
     * <p>
     * The range of values supported by HINCRBY is limited to 64 bit signed integers.
     * <p>
     * <b>Time complexity:</b> O(1)
     *
     * @return Integer reply The new value at field after the increment operation.
     */
    public Long incrBy(F field, long value) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.hincrBy(hashName, finalField(field), value);
        }
    }

    /**
     * Increment the number stored at field in the hash at key by a double precision floating point
     * value. If key does not exist, a new key holding a hash is created. If field does not exist or
     * holds a string, the value is set to 0 before applying the operation. Since the value argument
     * is signed you can use this command to perform both increments and decrements.
     * <p>
     * The range of values supported by HINCRBYFLOAT is limited to double precision floating point
     * values.
     * <p>
     * <b>Time complexity:</b> O(1)
     *
     * @return Double precision floating point reply The new value at field after the increment
     * operation.
     */
    public Double incrByFloat(F field, double value) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.hincrByFloat(hashName, finalField(field), value);
        }
    }

    public void incrBy(Map<F, Long> incrMap) {
        if (incrMap.isEmpty()) {
            return;
        }
        try (Jedis jedis = redisPool.getJedisClient()) {
            val pipeline = jedis.pipelined();
            for (Map.Entry<F, Long> entry : incrMap.entrySet()) {
                pipeline.hincrBy(hashName, finalField(entry.getKey()), entry.getValue());
            }
            syncAndCheck(pipeline);
        }
    }

    public boolean compareAndSet(F field, V expect, V update) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            List<byte[]> finalKeys = Collections.singletonList(hashName);
            List<byte[]> finalArgs = Arrays.asList(finalField(field), finalValue(expect), finalValue(update));
            Object result = jedis.eval(toBytes("" +
                "local v = redis.call('hget', KEYS[1], ARGV[1]); " +
                "if (v == false or v == ARGV[2]) then " +
                "redis.call('hset', KEYS[1], ARGV[1], ARGV[3]); " +
                "return 'OK';" +
                "end" +
                ""), finalKeys, finalArgs);
            return "OK".equals(result);
        }
    }

    public void remove(F field) {
        remove(Collections.singletonList(field));
    }

    public void remove(List<F> fields) {
        if (fields.isEmpty()) {
            return;
        }
        try (Jedis jedis = redisPool.getJedisClient()) {
            jedis.hdel(hashName, finalFields(fields));
        }
    }

    /**
     * Test for existence of a specified field in a hash. <b>Time complexity:</b> O(1)
     *
     * @return Return 1 if the hash stored at key contains the specified field. Return 0 if the key is
     * not found or the field is not present.
     */
    public Boolean containsField(F field) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.hexists(hashName, finalField(field));
        }
    }

    /**
     * Return the number of items in a hash.
     * <p>
     * <b>Time complexity:</b> O(1)
     *
     * @return The number of entries (fields) contained in the hash stored at key. If the specified
     * key does not exist, 0 is returned assuming an empty hash.
     */
    public Long size() {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.hlen(hashName);
        }
    }

    /**
     * Return all the fields in a hash.
     * <p>
     * <b>Time complexity:</b> O(N), where N is the total number of entries
     *
     * @return All the fields names contained into a hash.
     */
    public Set<F> keys() {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return returnFields(jedis.hkeys(hashName));
        }
    }

    /**
     * Return all the values in a hash.
     * <p>
     * <b>Time complexity:</b> O(N), where N is the total number of entries
     *
     * @return All the fields values contained into a hash.
     */
    public List<V> values() {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return returnValues(jedis.hvals(hashName));
        }
    }

    /**
     * Return all the fields and associated values in a hash.
     * <p>
     * <b>Time complexity:</b> O(N), where N is the total number of entries
     *
     * @return All the fields and values contained into a hash.
     */
    public Map<F, V> getAll() {
        try (Jedis jedis = redisPool.getJedisClient()) {
            Map<F, V> result = Maps.newHashMap();
            final Map<byte[], byte[]> hgetAll = jedis.hgetAll(hashName);
            hgetAll.forEach((key, value) -> result.put(fieldType.fromString(toString(key)), returnValue(value)));
            return result;
        }
    }

    protected byte[] finalField(F field) {
        return toBytes(fieldType.toString(field));
    }

    protected byte[][] finalFields(List<F> fields) {
        byte[][] a = new byte[fields.size()][];
        for (int i = 0; i < fields.size(); i++) {
            a[i] = finalField(fields.get(i));
        }
        return a;
    }

    protected Map<byte[], byte[]> finalValueMap(Map<F, V> valueMap) {
        Map<byte[], byte[]> map = new HashMap<>(valueMap.size());
        for (Map.Entry<F, V> entry : valueMap.entrySet()) {
            map.put(finalField(entry.getKey()), finalValue(entry.getValue()));
        }
        return map;
    }

    protected ScanResult<Map.Entry<F, V>> returnEntryScanResult(ScanResult<Map.Entry<byte[], byte[]>> result) {
        return new ScanResult<>(result.getCursorAsBytes(), returnEntries(result.getResult()));
    }

    protected List<Map.Entry<F, V>> returnEntries(List<Map.Entry<byte[], byte[]>> entries) {
        return map(entries, entry -> new Entry<F, V>() {
            @Override
            public F getKey() {
                return fieldType.fromString(RedisHash.toString(entry.getKey()));
            }

            @Override
            public V getValue() {
                return valueType.fromBytes(entry.getValue());
            }

            @Override
            public V setValue(V value) {
                throw new UnsupportedOperationException();
            }
        });
    }

    protected Set<F> returnFields(Set<byte[]> fields) {
        Set<String> fieldList = fields.stream().map(Object::toString).collect(Collectors.toSet());
        return map(fieldList, fieldType::fromString);
    }

    protected static <T, R> List<R> map(List<T> list, Function<T, R> f) {
        List<R> result = new ArrayList<>(list.size());
        for (T t : list) {
            result.add(f.apply(t));
        }
        return result;
    }

    protected static <T, R> Set<R> map(Set<T> set, Function<T, R> f) {
        Set<R> result = new HashSet<>(set.size());
        for (T t : set) {
            result.add(f.apply(t));
        }
        return result;
    }
}

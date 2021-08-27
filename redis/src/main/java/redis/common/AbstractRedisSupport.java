package redis.common;

import redis.pool.RedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.exceptions.JedisDataException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

abstract class AbstractRedisSupport<K, V> {

    private static final Pattern KEY_PATTERN = Pattern.compile("[a-zA-Z_0-9\\.\\-:]+(/[a-zA-Z_0-9\\.\\-:]+)*");

    protected static Charset ENCODING = StandardCharsets.UTF_8;

    protected final byte[] keyPrefix;
    protected final String keySpace;
    protected final RedisKeyType<K> keyType;
    protected final RedisValueType<V> valueType;
    protected final RedisPool redisPool;

    protected AbstractRedisSupport(byte[] keyPrefix,
                                   String keySpace,
                                   RedisKeyType<K> keyType,
                                   RedisValueType<V> valueType,
                                   RedisPool redisPool) {
        this.keyPrefix = keyPrefix;
        this.keySpace = keySpace;
        if (keyType == null) {
            throw new IllegalArgumentException("key type is not set");
        }
        this.keyType = keyType;
        if (valueType == null) {
            throw new IllegalArgumentException("value type is not set");
        }
        this.valueType = valueType;
        this.redisPool = redisPool;
    }

    protected AbstractRedisSupport(String keySpace, RedisKeyType<K> keyType, RedisValueType<V> valueType, RedisPool redisPool) {
        this(keySpaceToKeyPrefix(keySpace), keySpace, keyType, valueType, redisPool);
    }

    protected byte[] finalKey(K key) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be empty");
        }

        String evaluatedKey = keyType.toString(key);
        if (evaluatedKey == null || evaluatedKey.isEmpty()) {
            throw new IllegalArgumentException("got empty key from " + key);
        }
        return finalKey(evaluatedKey);
    }

    protected byte[] finalKey(String key) {
        return finalKey(key.getBytes(ENCODING));
    }

    protected byte[] finalKey(byte[] key) {
        if (key == null || key.length == 0) {
            throw new IllegalArgumentException("key must not be empty");
        }
        if (keyPrefix == null) {
            return key;
        } else {
            byte[] bytes = new byte[keyPrefix.length + key.length];
            System.arraycopy(keyPrefix, 0, bytes, 0, keyPrefix.length);
            System.arraycopy(key, 0, bytes, keyPrefix.length, key.length);
            return bytes;
        }
    }

    protected List<byte[]> finalKeyList(List<K> keys) {
        return map(keys, this::finalKey);
    }

    protected byte[][] finalKeys(List<K> keys) {
        byte[][] finalKeys = new byte[keys.size()][];
        for (int i = 0; i < finalKeys.length; i++) {
            finalKeys[i] = finalKey(keys.get(i));
        }
        return finalKeys;
    }

    protected byte[][] finalKeys(K[] keys) {
        byte[][] finalKeys = new byte[keys.length][];
        for (int i = 0; i < finalKeys.length; i++) {
            finalKeys[i] = finalKey(keys[i]);
        }
        return finalKeys;
    }

    protected byte[][] finalKeys(String[] keys) {
        return map(keys, this::finalKey, new byte[0][]);
    }

    protected String finalStringKey(K key) {
        return toString(finalKey(key));
    }

    protected String finalStringKey(String key) {
        return toString(finalKey(key.getBytes(ENCODING)));
    }

    protected List<String> finalStringKeys(List<K> keys) {
        return map(keys, this::finalStringKey);
    }

    protected String[] finalStringKeys(String[] keys) {
        return map(keys, this::finalStringKey, new String[0]);
    }

    protected List<byte[]> finalEvalArgs(List<String> args, List<V> values) {
        if (args.isEmpty()) {
            if (values.isEmpty()) {
                return Collections.emptyList();
            }
            return map(values, this::finalValue);
        }
        if (values.isEmpty()) {
            return map(args, AbstractRedisSupport::toBytes);
        }
        List<byte[]> list = new ArrayList<>(values.size() + args.size());
        args.forEach(s -> list.add(toBytes(s)));
        values.forEach(v -> list.add(finalValue(v)));
        return list;
    }

    protected String originKey(String finalKey) {
        return originKey(toBytes(finalKey));
    }

    protected String originKey(byte[] finalKey) {
        byte[] bytes;
        if (keyPrefix == null) {
            bytes = finalKey;
        } else {
            bytes = new byte[finalKey.length - keyPrefix.length];
            System.arraycopy(finalKey, keyPrefix.length, bytes, 0, bytes.length);
        }
        return toString(bytes);
    }

    protected Set<String> originKeys(Set<String> finalKeys) {
        return map(finalKeys, this::originKey);
    }

    protected List<String> originKeys(List<String> finalKeys) {
        return map(finalKeys, this::originKey);
    }

    protected byte[][] finalValues(V[] values) {
        byte[][] bytes = new byte[values.length][];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = finalValue(values[i]);
        }
        return bytes;
    }

    protected byte[][] finalValues(List<V> values) {
        byte[][] bytes = new byte[values.size()][];
        for (int i = 0; i < values.size(); i++) {
            bytes[i] = finalValue(values.get(i));
        }
        return bytes;
    }

    protected byte[] finalValue(V value) {
        return valueType.toBytes(value);
    }

    protected byte[][] finalKeysValues(Map<K, V> keyValues) {
        byte[][] finalKeyValues = new byte[keyValues.size()][];
        int n = 0;
        for (Map.Entry<K, V> entry : keyValues.entrySet()) {
            finalKeyValues[n++] = finalKey(entry.getKey());
            finalKeyValues[n++] = finalValue(entry.getValue());
        }
        return finalKeyValues;
    }

    protected Map<byte[], byte[]> finalHash(Map<String, V> hash) {
        Map<byte[], byte[]> map = new HashMap<>(hash.size());
        for (Map.Entry<String, V> entry : hash.entrySet()) {
            map.put(toBytes(entry.getKey()), finalValue(entry.getValue()));
        }
        return map;
    }

    protected <T> Map<byte[], T> finalValuesInMapKey(Map<V, T> map) {
        Map<byte[], T> result = new HashMap<>(map.size());
        for (Map.Entry<V, T> entry : map.entrySet()) {
            result.put(finalValue(entry.getKey()), entry.getValue());
        }
        return result;
    }

    protected ScanParams finalKeyScanParams(int count, String[] patterns) {
        ScanParams params = new ScanParams();
        for (String pattern : patterns) {
            params.match(finalKey(pattern));
        }
        if (count > 0) {
            params.count(count);
        }
        return params;
    }

    protected ScanParams finalScanParams(int count, String[] patterns) {
        ScanParams params = new ScanParams();
        for (String pattern : patterns) {
            params.match(pattern);
        }
        if (count > 0) {
            params.count(count);
        }
        return params;
    }

    protected V returnValue(byte[] value) {
        return valueType.fromBytes(value);
    }

    protected List<V> returnValues(List<byte[]> values) {
        List<V> list = new ArrayList<>(values.size());
        for (byte[] value : values) {
            list.add(returnValue(value));
        }
        return list;
    }

    protected List<V> stringReturnValues(List<String> values) {
        List<V> list = new ArrayList<>(values.size());
        for (String value : values) {
            list.add(value == null ? null : returnValue(value.getBytes(ENCODING)));
        }
        return list;
    }

    protected Set<V> returnValues(Set<byte[]> values) {
        Set<V> set = new HashSet<>(values.size());
        for (byte[] value : values) {
            set.add(returnValue(value));
        }
        return set;
    }

    protected Map<String, V> returnHash(Map<byte[], byte[]> hash) {
        Map<String, V> map = new HashMap<>(hash.size());
        for (Map.Entry<byte[], byte[]> entry : hash.entrySet()) {
            map.put(toString(entry.getKey()), returnValue(entry.getValue()));
        }
        return map;
    }

    protected Set<String> returnSet(Set<byte[]> set) {
        Set<String> result = new HashSet<>(set.size());
        for (byte[] bytes : set) {
            result.add(toString(bytes));
        }
        return result;
    }

    protected ScanResult<Map.Entry<String, V>> returnEntryScanResult(ScanResult<Map.Entry<byte[], byte[]>> result) {
        return new ScanResult<>(result.getCursorAsBytes(), returnEntries(result.getResult()));
    }

    protected List<Map.Entry<String, V>> returnEntries(List<Map.Entry<byte[], byte[]>> entries) {
        return map(entries, entry -> new Map.Entry<String, V>() {
            @Override
            public String getKey() {
                return AbstractRedisSupport.toString(entry.getKey());
            }

            @Override
            public V getValue() {
                return returnValue(entry.getValue());
            }

            @Override
            public V setValue(V value) {
                return null;
            }
        });
    }

    protected ScanResult<V> returnScanResult(ScanResult<byte[]> result) {
        return new ScanResult<>(result.getCursorAsBytes(), returnValues(result.getResult()));
    }

    protected Map<K, V> returnKeyValue(K key, List<byte[]> keysValues) {
        if (keysValues.size() < 2) {
            return Collections.emptyMap();
        } else {
            return Collections.singletonMap(key, returnValue(keysValues.get(1)));
        }
    }

    protected Map<K, V> returnKeysValues(K[] keys, List<byte[]> keysValues) {
        Map<K, V> map = new HashMap<>(keysValues.size() / 2);
        byte[][] finalKeys = finalKeys(keys);
        for (int i = 0; i < keysValues.size(); i += 2) {
            byte[] finalKey = keysValues.get(i);
            map.put(keys[indexOf(finalKeys, finalKey)], returnValue(keysValues.get(i + 1)));
        }
        return map;
    }

    protected static byte[] toBytes(String text) {
        return text == null ? null : text.getBytes(ENCODING);
    }

    protected static byte[][] toBytes(List<String> texts) {
        byte[][] result = new byte[texts.size()][];
        for (int i = 0; i < texts.size(); i++) {
            result[i] = toBytes(texts.get(i));
        }
        return result;
    }

    protected static byte[][] toBytes(String[] texts) {
        byte[][] result = new byte[texts.length][];
        for (int i = 0; i < texts.length; i++) {
            result[i] = toBytes(texts[i]);
        }
        return result;
    }

    protected static String toString(byte[] bytes) {
        return bytes == null ? null : new String(bytes, ENCODING);
    }

    protected static Set<String> toString(Set<byte[]> set) {
        Set<String> result = new HashSet<>(set.size());
        for (byte[] bytes : set) {
            result.add(toString(bytes));
        }
        return result;
    }

    protected static List<String> toString(List<byte[]> set) {
        List<String> result = new ArrayList<>(set.size());
        for (byte[] bytes : set) {
            result.add(toString(bytes));
        }
        return result;
    }

    protected static <T, R> R[] map(T[] a, Function<T, R> f, R[] t) {
        R[] rs = Arrays.copyOf(t, a.length);
        for (int i = 0; i < a.length; i++) {
            rs[i] = f.apply(a[i]);
        }
        return rs;
    }

    protected static <T, R> List<R> map(List<T> list, Function<T, R> f) {
        List<R> result = new ArrayList<>(list.size());
        for (T t : list) {
            result.add(f.apply(t));
        }
        return result;
    }

    protected static <T, R> Set<R> map(Set<T> list, Function<T, R> f) {
        Set<R> result = new HashSet<>(list.size());
        for (T t : list) {
            result.add(f.apply(t));
        }
        return result;
    }

    protected static int indexOf(byte[][] a, byte[] t) {
        for (int i = 0; i < a.length; i++) {
            if (Arrays.equals(a[i], t)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 同步所有操作，然后对操作结果进行检查，并抛出第一个遇到的redis异常（如果存在）
     */
    protected void syncAndCheck(Pipeline pipeline) {
        for (Object o : pipeline.syncAndReturnAll()) {
            if (o instanceof JedisDataException) {
                throw (JedisDataException)o;
            }
        }
    }

    private static byte[] keySpaceToKeyPrefix(String keySpace) {
        if (keySpace == null) {
            return null;
        }
        if (!KEY_PATTERN.matcher(keySpace).matches()) {
            throw new IllegalArgumentException("invalid key space " + keySpace +
                ", must match pattern " + KEY_PATTERN.pattern());
        }
        return (keySpace + "/").getBytes(ENCODING);
    }

}

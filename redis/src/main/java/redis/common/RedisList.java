package redis.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ListPosition;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.pool.RedisPool;

@SuppressWarnings("unchecked")
public class RedisList<K, V> extends AbstractRedisSupport<K, V> {

    private final K redisKey;

    public RedisList(K redisKey,
                     RedisPool redisPool,
                     String keySpace,
                     RedisValueType<V> valueType) {
        super(keySpace, new DefaultRedisKeyType<>((Class<K>)redisKey.getClass()), valueType, redisPool);
        this.redisKey = redisKey;
    }

    /**
     * Add the string value to the head (LPUSH) or tail (RPUSH) of the list stored at key. If the key
     * does not exist an empty list is created just before the append operation. If the key exists but
     * is not a List an error is returned.
     * <p>
     * Time complexity: O(1)
     *
     * @return Integer reply, specifically, the number of elements inside the list after the push
     * operation.
     */
    public Long rpush(V value) {
        return rpush(Collections.singletonList(value));
    }

    /**
     * Add the string value to the head (LPUSH) or tail (RPUSH) of the list stored at key. If the key
     * does not exist an empty list is created just before the append operation. If the key exists but
     * is not a List an error is returned.
     * <p>
     * Time complexity: O(1)
     *
     * @return Integer reply, specifically, the number of elements inside the list after the push
     * operation.
     */
    public Long rpush(List<V> values) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.rpush(finalKey(redisKey), finalValues(values));
        }
    }

    /**
     * Add the string value to the head (LPUSH) or tail (RPUSH) of the list stored at key. If the key
     * does not exist an empty list is created just before the append operation. If the key exists but
     * is not a List an error is returned.
     * <p>
     * Time complexity: O(1)
     *
     * @return Integer reply, specifically, the number of elements inside the list after the push
     * operation.
     */
    public Long lpush(V value) {
        return lpush(Collections.singletonList(value));
    }

    /**
     * Add the string value to the head (LPUSH) or tail (RPUSH) of the list stored at key. If the key
     * does not exist an empty list is created just before the append operation. If the key exists but
     * is not a List an error is returned.
     * <p>
     * Time complexity: O(1)
     *
     * @return Integer reply, specifically, the number of elements inside the list after the push
     * operation.
     */
    public Long lpush(List<V> values) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.lpush(finalKey(redisKey), finalValues(values));
        }
    }

    /**
     * Return the length of the list stored at the specified key. If the key does not exist zero is
     * returned (the same behaviour as for empty lists). If the value stored at key is not a list an
     * error is returned.
     * <p>
     * Time complexity: O(1)
     *
     * @return The length of the list.
     */
    public Long len() {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.llen(finalKey(redisKey));
        }
    }

    /**
     * Return the specified elements of the list stored at the specified key. Start and end are
     * zero-based indexes. 0 is the first element of the list (the list head), 1 the next element and
     * so on.
     * <p>
     * For example LRANGE foobar 0 2 will return the first three elements of the list.
     * <p>
     * start and end can also be negative numbers indicating offsets from the end of the list. For
     * example -1 is the last element of the list, -2 the penultimate element and so on.
     * <p>
     * <b>Consistency with range functions in various programming languages</b>
     * <p>
     * Note that if you have a list of numbers from 0 to 100, LRANGE 0 10 will return 11 elements,
     * that is, rightmost item is included. This may or may not be consistent with behavior of
     * range-related functions in your programming language of choice (think Ruby's Range.new,
     * Array#slice or Python's range() function).
     * <p>
     * LRANGE behavior is consistent with one of Tcl.
     * <p>
     * <b>Out-of-range indexes</b>
     * <p>
     * Indexes out of range will not produce an error: if start is over the end of the list, or start
     * &gt; end, an empty list is returned. If end is over the end of the list Redis will threat it
     * just like the last element of the list.
     * <p>
     * Time complexity: O(start+n) (with n being the length of the range and start being the start
     * offset)
     *
     * @return Multi bulk reply, specifically a list of elements in the specified range.
     */
    public List<V> range(long start, long end) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return returnValues(jedis.lrange(finalKey(redisKey), start, end));
        }
    }

    /**
     * Trim an existing list so that it will contain only the specified range of elements specified.
     * Start and end are zero-based indexes. 0 is the first element of the list (the list head), 1 the
     * next element and so on.
     * <p>
     * For example LTRIM foobar 0 2 will modify the list stored at foobar key so that only the first
     * three elements of the list will remain.
     * <p>
     * start and end can also be negative numbers indicating offsets from the end of the list. For
     * example -1 is the last element of the list, -2 the penultimate element and so on.
     * <p>
     * Indexes out of range will not produce an error: if start is over the end of the list, or start
     * &gt; end, an empty list is left as value. If end over the end of the list Redis will threat it
     * just like the last element of the list.
     * <p>
     * Hint: the obvious use of LTRIM is together with LPUSH/RPUSH. For example:
     * <p>
     * {@code lpush("mylist", "someelement"); ltrim("mylist", 0, 99); * }
     * <p>
     * The above two commands will push elements in the list taking care that the list will not grow
     * without limits. This is very useful when using Redis to store logs for example. It is important
     * to note that when used in this way LTRIM is an O(1) operation because in the average case just
     * one element is removed from the tail of the list.
     * <p>
     * Time complexity: O(n) (with n being len of list - len of range)
     *
     * @return Status code reply
     */
    public String trim(long start, long end) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.ltrim(finalKey(redisKey), start, end);
        }
    }

    /**
     * Return the specified element of the list stored at the specified key. 0 is the first element, 1
     * the second and so on. Negative indexes are supported, for example -1 is the last element, -2
     * the penultimate and so on.
     * <p>
     * If the value stored at key is not of list type an error is returned. If the index is out of
     * range a 'nil' reply is returned.
     * <p>
     * Note that even if the average time complexity is O(n) asking for the first or the last element
     * of the list is O(1).
     * <p>
     * Time complexity: O(n) (with n being the length of the list)
     *
     * @return Bulk reply, specifically the requested element
     */
    public V index(long index) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return returnValue(jedis.lindex(finalKey(redisKey), index));
        }
    }

    /**
     * Set a new value as the element at index position of the List at key.
     * <p>
     * Out of range indexes will generate an error.
     * <p>
     * Similarly to other list commands accepting indexes, the index can be negative to access
     * elements starting from the end of the list. So -1 is the last element, -2 is the penultimate,
     * and so forth.
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(N) (with N being the length of the list), setting the first or last elements of the list is
     * O(1).
     *
     * @return Status code reply
     * @see #index(long)
     */
    public String set(long index, V value) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.lset(finalKey(redisKey), index, finalValue(value));
        }
    }

    /**
     * Remove the first count occurrences of the value element from the list. If count is zero all the
     * elements are removed. If count is negative elements are removed from tail to head, instead to
     * go from head to tail that is the normal behaviour. So for example LREM with count -2 and hello
     * as value to remove against the list (a,b,c,hello,x,hello,hello) will lave the list
     * (a,b,c,hello,x). The number of removed elements is returned as an integer, see below for more
     * information about the returned value. Note that non existing keys are considered like empty
     * lists by LREM, so LREM against non existing keys will always return 0.
     * <p>
     * Time complexity: O(N) (with N being the length of the list)
     *
     * @return Integer Reply, specifically: The number of removed elements if the operation succeeded
     */
    public Long rem(long count, V value) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.lrem(finalKey(redisKey), count, finalValue(value));
        }
    }

    /**
     * Atomically return and remove the first (LPOP) or last (RPOP) element of the list. For example
     * if the list contains the elements "a","b","c" LPOP will return "a" and the list will become
     * "b","c".
     * <p>
     * If the key does not exist or the list is already empty the special value 'nil' is returned.
     *
     * @return Bulk reply
     * @see #rpop()
     */
    public V lpop() {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return returnValue(jedis.lpop(finalKey(redisKey)));
        }
    }

    /**
     * Atomically return and remove the first (LPOP) or last (RPOP) element of the list. For example
     * if the list contains the elements "a","b","c" LPOP will return "a" and the list will become
     * "b","c".
     * <p>
     * If the key does not exist or the list is already empty the special value 'nil' is returned.
     *
     * @return Bulk reply
     * @see #rpop()
     */
    public List<V> lpop(int count) {
        return pop(count, "" +
            "local list = {}; " +
            "for i=1,ARGV[1] do " +
            "local v = redis.call('lpop', KEYS[1]);" +
            "if (v == false) then return list; else table.insert(list, v); end " +
            "end " +
            "return list;" +
            "");
    }

    /**
     * Atomically return and remove the first (LPOP) or last (RPOP) element of the list. For example
     * if the list contains the elements "a","b","c" RPOP will return "c" and the list will become
     * "a","b".
     * <p>
     * If the key does not exist or the list is already empty the special value 'nil' is returned.
     *
     * @return Bulk reply
     * @see #lpop()
     */
    public V rpop() {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return returnValue(jedis.rpop(finalKey(redisKey)));
        }
    }

    /**
     * Atomically return and remove the first (LPOP) or last (RPOP) element of the list. For example
     * if the list contains the elements "a","b","c" RPOP will return "c" and the list will become
     * "a","b".
     * <p>
     * If the key does not exist or the list is already empty the special value 'nil' is returned.
     *
     * @return Bulk reply
     * @see #lpop()
     */
    public List<V> rpop(int count) {
        return pop(count, "" +
            "local list = {}; " +
            "for i=1,ARGV[1] do " +
            "local v = redis.call('rpop', KEYS[1]);" +
            "if (v == false) then return list; else table.insert(list, v); end " +
            "end " +
            "return list;" +
            "");
    }

    private List<V> pop(int count, String script) {
        if (count <= 0) {
            throw new IllegalArgumentException(count + " <= 0");
        }
        List<V> result = new ArrayList<>();
        int batchSize = 1000;
        try (Jedis jedis = redisPool.getJedisClient()) {
            for (int i = 0; i < count; i += batchSize) {
                int c = Math.min(batchSize, count - i);
                List<byte[]> finalKeys = finalKeyList(Collections.singletonList(redisKey));
                List<byte[]> finalArgs = finalEvalArgs(Collections.singletonList(String.valueOf(c)), Collections.emptyList());
                Object o;
                try {
                    o = evalResult(jedis.evalsha(toBytes(scriptSha(script)), finalKeys, finalArgs));
                } catch (JedisDataException e) {
                    if (e.getMessage() != null && e.getMessage().contains("NOSCRIPT")) {
                        o = evalResult(jedis.eval(toBytes(script), finalKeys, finalArgs));
                    } else {
                        throw e;
                    }
                }
                //noinspection unchecked
                List<V> batch = stringReturnValues((List<String>)o);
                if (batch.size() < c) {
                    if (result.isEmpty()) {
                        // fast path
                        return batch;
                    }
                    result.addAll(batch);
                    break;
                } else {
                    result.addAll(batch);
                }
            }
        }
        return result;
    }

    public Long lpushx(List<V> values) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.lpushx(finalKey(redisKey), finalValues(values));
        }
    }

    public Long rpushx(List<V> values) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.rpushx(finalKey(redisKey), finalValues(values));
        }
    }

    public Long insert(ListPosition where, String pivot, V value) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.linsert(finalKey(redisKey), where, toBytes(pivot), finalValue(value));
        }
    }

    public void clear() {
        try (Jedis jedis = redisPool.getJedisClient()) {
            jedis.del(finalKey(redisKey));
        }
    }
}

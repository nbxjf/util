package redis.common;

import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.pool.RedisPool;

@SuppressWarnings("unchecked")
public class RedisSet<K, V> extends AbstractRedisSupport<K, V> {

    private final K redisKey;

    public RedisSet(RedisPool redisPool,
                    String setPrefix,
                    K redisKey,
                    RedisValueType<V> valueType) {
        super(setPrefix, new RedisKeyInstance<>((Class<K>)redisKey.getClass()), valueType, redisPool);
        this.redisKey = redisKey;
    }

    /**
     * Add the specified member to the set value stored at key. If member is already a member of the
     * set no operation is performed. If key does not exist a new set with the specified member as
     * sole member is created. If the key exists but does not hold a set value an error is returned.
     * <p>
     * Time complexity O(1)
     *
     * @return Integer reply, specifically: 1 if the new element was added 0 if the element was
     * already a member of the set
     */
    public Long add(List<V> members) {
        if (members.isEmpty()) {
            return 0L;
        }
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.sadd(finalKey(redisKey), finalValues(members));
        }
    }

    /**
     * Return all the members (elements) of the set value stored at key.
     * <p>
     * Time complexity O(N)
     *
     * @return Multi bulk reply
     */
    public Set<V> members() {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return returnValues(jedis.smembers(finalKey(redisKey)));
        }
    }

    /**
     * Remove the specified member from the set value stored at key. If member was not a member of the
     * set no operation is performed. If key does not hold a set value an error is returned.
     * <p>
     * Time complexity O(1)
     *
     * @return Integer reply, specifically: 1 if the new element was removed 0 if the new element was
     * not a member of the set
     */
    public Long rem(List<V> members) {
        if (members.isEmpty()) {
            return 0L;
        }
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.srem(finalKey(redisKey), finalValues(members));
        }
    }

    /**
     * Remove a random element from a Set returning it as return value. If the Set is empty or the key
     * does not exist, a nil object is returned.
     * <p>
     * The {@link #randMember()} command does a similar work but the returned element is not
     * removed from the Set.
     * <p>
     * Time complexity O(1)
     *
     * @return Bulk reply
     */
    public V pop() {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return returnValue(jedis.spop(finalKey(redisKey)));
        }
    }

    public Set<V> pop(long count) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return returnValues(jedis.spop(finalKey(redisKey), count));
        }
    }

    /**
     * Move the specifided member from the set at srckey to the set at dstkey. This operation is
     * atomic, in every given moment the element will appear to be in the source or destination set
     * for accessing clients.
     * <p>
     * If the source set does not exist or does not contain the specified element no operation is
     * performed and zero is returned, otherwise the element is removed from the source set and added
     * to the destination set. On success one is returned, even if the element was already present in
     * the destination set.
     * <p>
     * An error is raised if the source or destination keys contain a non Set value.
     * <p>
     * Time complexity O(1)
     *
     * @return Integer reply, specifically: 1 if the element was moved 0 if the element was not found
     * on the first set and no operation was performed
     */
    public Long move(String targetSet, V member) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.smove(finalKey(redisKey), toBytes(targetSet), finalValue(member));
        }
    }

    /**
     * Return the set cardinality (number of elements). If the key does not exist 0 is returned, like
     * for empty sets.
     *
     * @return Integer reply, specifically: the cardinality (number of elements) of the set as an
     * integer.
     */
    public Long card() {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.scard(finalKey(redisKey));
        }
    }

    /**
     * Return 1 if member is a member of the set stored at key, otherwise 0 is returned.
     * <p>
     * Time complexity O(1)
     *
     * @return Integer reply, specifically: 1 if the element is a member of the set 0 if the element
     * is not a member of the set OR if the key does not exist
     */
    public Boolean contains(V member) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return jedis.sismember(finalKey(redisKey), finalValue(member));
        }
    }

    /**
     * Return a random element from a Set, without removing the element. If the Set is empty or the
     * key does not exist, a nil object is returned.
     * <p>
     * The SPOP command does a similar work but the returned element is popped (removed) from the Set.
     * <p>
     * Time complexity O(1)
     *
     * @return Bulk reply
     */
    public V randMember() {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return returnValue(jedis.srandmember(finalKey(redisKey)));
        }
    }

    public List<V> randMember(int count) {
        try (Jedis jedis = redisPool.getJedisClient()) {
            return returnValues(jedis.srandmember(finalKey(redisKey), count));
        }
    }
}

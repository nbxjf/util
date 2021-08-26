package pool;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPool implements AutoCloseable {

    private final JedisPool jedisPool;

    public RedisPool(RedisConfig redisConfig) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        if (redisConfig.getMaxIdle() > 0) {
            jedisPoolConfig.setMaxIdle(redisConfig.getMaxIdle());
        }
        if (redisConfig.getMaxTotal() > 0) {
            jedisPoolConfig.setMaxTotal(redisConfig.getMaxTotal());
        }
        jedisPoolConfig.setMaxWaitMillis(Math.max(0, redisConfig.getMaxWaitMillis()));
//        如果为true（默认为false），当应用向连接池申请连接时，连接池会判断这条连接是否是可用的
        jedisPoolConfig.setTestOnBorrow(redisConfig.isTestOnBorrow());
        jedisPoolConfig.setTestOnCreate(redisConfig.isTestOnCreate());
        jedisPoolConfig.setTestOnReturn(redisConfig.isTestOnReturn());
        jedisPool = new JedisPool(jedisPoolConfig, redisConfig.getHost(), redisConfig.getPort(), redisConfig.getTimeout(), redisConfig.getPassword(), redisConfig.getDatabase());
    }

    /**
     * 从连接池中获取一个jedis连接
     *
     * @return jedis
     */
    public Jedis getJedisClient() {
        return jedisPool.getResource();
    }

    @Override
    public void close() throws Exception {
        jedisPool.close();
    }
}

package redis.common;

import redis.pool.RedisConfig;
import redis.pool.RedisPool;
import utils.serializer.FastJsonSerializer;

/**
 * Created by Jeff_xu on 2021/8/27.
 *
 * @author Jeff_xu
 */
public class RedisMapTest {

    public static void main(String[] args) {
        RedisConfig redisConfig = new RedisConfig();
        redisConfig.setHost("127.0.0.1");
        redisConfig.setDatabase(4);
        redisConfig.setPassword("123456");
        RedisPool redisPool = new RedisPool(redisConfig);

        RedisMap<String, Long> map = new RedisMap<>(redisPool, "test", RedisKeyType.normal(), new DefaultRedisValueType<>(Long.class, new FastJsonSerializer()));
        map.set("longValue", 5556456456456456455L);

        Long longValue = map.get("longValue");
        System.out.println(longValue);
    }
}

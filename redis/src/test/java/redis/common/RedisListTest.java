package redis.common;

import org.junit.Test;
import redis.pool.RedisConfig;
import redis.pool.RedisPool;
import utils.serializer.FastJsonSerializer;

/**
 * Created by Jeff_xu on 2021/8/27.
 *
 * @author Jeff_xu
 */
public class RedisListTest {

    @Test
    public void testLpush() {
        RedisConfig redisConfig = new RedisConfig();
        redisConfig.setHost("127.0.0.1");
        redisConfig.setDatabase(4);
        redisConfig.setPassword("123456");
        RedisPool redisPool = new RedisPool(redisConfig);

        String key = "list";
        final RedisList<String, String> list = new RedisList<String, String>(key, redisPool, "test", RedisKeyType.STRING, new DefaultRedisValueType<>(String.class, new FastJsonSerializer()));
        list.lpush("1");
        list.lpush("2");
        System.out.println(list.lpop());
        System.out.println(list.lpop());
    }
}

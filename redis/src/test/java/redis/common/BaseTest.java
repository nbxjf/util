package redis.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Before;
import redis.pool.RedisConfig;
import redis.pool.RedisPool;

/**
 * Created by Jeff_xu on 2021/8/31.
 *
 * @author Jeff_xu
 */
public class BaseTest {
    protected RedisPool redisPool;

    @Before
    public void setUp() {
        RedisConfig redisConfig = new RedisConfig();
        redisConfig.setHost("127.0.0.1");
        redisConfig.setDatabase(4);
        redisConfig.setPassword("123456");
        redisPool = new RedisPool(redisConfig);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Student {
        private String name;
        private int level;

        @Override
        public String toString() {
            return name;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Men {
        private String name;
        private int level;

    }
}

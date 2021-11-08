package redis.bloomfilter;

import io.github.dengliming.redismodule.redisbloom.BloomFilter;
import io.github.dengliming.redismodule.redisbloom.client.RedisBloomClient;
import org.redisson.config.Config;

/**
 * Created by Jeff_xu on 2021/10/19.
 * 布隆过滤器
 *
 * @author Jeff_xu
 */
public class RedisBloomFilter {

    private static Config config;

    static {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
    }

    /**
     * 创建一个布隆过滤器
     *
     * @param name      名称
     * @param errorRate 错误率
     * @param capacity  容积
     * @return 布隆过滤器
     */
    public BloomFilter createBloomFilter(String name, double errorRate, long capacity) {
        RedisBloomClient redisBloomClient = new RedisBloomClient(config);
        BloomFilter rBloomFilter = redisBloomClient.getRBloomFilter(name);
        rBloomFilter.create(errorRate, capacity);
        return rBloomFilter;
    }

}

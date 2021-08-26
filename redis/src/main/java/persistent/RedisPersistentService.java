package persistent;

import pool.RedisPool;

public class RedisPersistentService {

    private final RedisPool redisPool;
    private final String keySpace;

    /**
     * 构造一个redis表服务
     *
     * @param pool 构造一个redis表服务需要的redis连接池
     */
    protected RedisPersistentService(RedisPool pool) {
        this.redisPool = pool;
        this.keySpace = "persistent";
    }
}

package pool;

import lombok.Data;
import redis.clients.jedis.Protocol;

/**
 * 提供redis的连接信息配置
 */
@Data
public class RedisConfig {
    /**
     * Redis的服务器地址
     */
    private String host;
    /**
     * redis的端口号
     * 默认6379
     */
    private short port = Protocol.DEFAULT_PORT;
    /**
     * redis链接的密码
     */
    private String password="";
    /**
     * redis链接的db编号
     */
    private int database;
    /**
     * 超时时间
     */
    private int timeout = Protocol.DEFAULT_TIMEOUT;
    /**
     * redis连接池的最大非活跃数量
     */
    private int maxIdle = 8;
    /**
     * redis连接池的最大数量
     */
    private int maxTotal = 8;
    /**
     * 在从redis连接池获取redis连接实例时最多允许等待的毫秒数
     */
    private long maxWaitMillis = Protocol.DEFAULT_TIMEOUT;

    private boolean testOnBorrow = false;

    private boolean testOnCreate = false;

    private boolean testOnReturn = false;

}

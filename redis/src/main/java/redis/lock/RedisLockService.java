package redis.lock;

import java.util.Collections;
import java.util.List;

import redis.common.RedisKeyType;
import redis.pool.RedisPool;

/*
基于redis的分布式锁服务

对象的声明过程如下：
1. 在spring配置文件中增加如下bean定义

    <bean id="businessRedisConfig" class="com.yit.common.utils.redis.RedisConfig">
        <property name="host" value="${business.redis.host}"/>
        <property name="port" value="${business.redis.port}"/>
        <property name="password" value="${business.redis.password}"/>
        <property name="database" value="${business.redis.database}"/>
    </bean>
    <bean id="businessRedisPool" class="com.yit.common.utils.redis.RedisPool">
        <property name="config" ref="businessRedisConfig"/>
    </bean>
    <bean id="businessRedisLockService" class="com.yit.common.utils.redis.RedisLockService">
        <constructor-arg name="pool" ref="businessRedisPool"/>
    </bean>

2. 在需要使用redis持久化服务的类中注入 businessRedisLockService
 */

/**
 * 基于redis的分布式锁服务
 */
public class RedisLockService {

    private final RedisPool redisPool;

    public RedisLockService(RedisPool redisPool) {
        this.redisPool = redisPool;
    }

    /**
     * 构造一个基于redis的分布式锁
     * redis锁的本质是在给定的时间范围内最早设置给定key对应的节点的值
     * 主要用于避免并发操作
     *
     * @param scope            锁所属的业务范围
     * @param key              要申请的锁的名称
     * @param expiryTimeMillis 锁的存活期，单位毫秒，存活期需要长于500毫秒短于一个小时
     * @param <K>              key类型
     * @return 返回redis锁代理对象
     */
    public <K> RedisLock buildLock(String scope, K key, long expiryTimeMillis) {
        return buildLock(scope, key, RedisKeyType.normal(), expiryTimeMillis);
    }

    /**
     * 构造一个基于redis的分布式锁
     * redis锁的本质是在给定的时间范围内最早设置给定key对应的节点的值
     * 主要用于避免并发操作
     *
     * @param scope            锁所属的业务范围
     * @param key              要申请的锁的名称
     * @param keyType          key类型描述
     * @param expiryTimeMillis 锁的存活期，单位毫秒，存活期需要长于500毫秒短于一个小时
     * @param <K>              key类型
     * @return 返回redis锁代理对象
     */
    public <K> RedisLock buildLock(String scope, K key, RedisKeyType<K> keyType, long expiryTimeMillis) {
        return new RedisReentrantLockImpl<>(redisPool, keyType, Collections.singletonList(key), expiryTimeMillis, scope);
    }

    /**
     * 构造一个基于redis的分布式锁
     * redis锁的本质是在给定的时间范围内最早设置给定key对应的节点的值
     * 主要用于避免并发操作
     * 用于同时申请一批redis锁，如果有一个锁申请不成功，整个申请过程会回滚，并汇报锁申请失败
     *
     * @param scope            锁所属的业务范围
     * @param keys             要申请的锁的名称列表，当且仅当所有的key都申请成功后此redis锁才获取成功
     * @param expiryTimeMillis 锁的存活期，单位毫秒，存活期需要长于500毫秒短于一个小时
     * @param <K>              key类型
     * @return 返回redis锁代理对象
     */
    public <K> RedisLock buildLock(String scope, List<K> keys, long expiryTimeMillis) {
        return buildLock(scope, keys, RedisKeyType.<K>normal(), expiryTimeMillis);
    }

    /**
     * 构造一个基于redis的分布式锁
     * redis锁的本质是在给定的时间范围内最早设置给定key对应的节点的值
     * 主要用于避免并发操作
     * 用于同时申请一批redis锁，如果有一个锁申请不成功，整个申请过程会回滚，并汇报锁申请失败
     *
     * @param scope            锁所属的业务范围
     * @param keys             要申请的锁的名称列表，当且仅当所有的key都申请成功后此redis锁才获取成功
     * @param keyType          key类型描述
     * @param expiryTimeMillis 锁的存活期，单位毫秒，存活期需要长于500毫秒短于一个小时
     * @param <K>              key类型
     * @return 返回redis锁代理对象
     */
    public <K> RedisLock buildLock(String scope, List<K> keys, RedisKeyType<K> keyType, long expiryTimeMillis) {
        return new RedisReentrantLockImpl<>(redisPool, keyType, keys, expiryTimeMillis, scope);
    }

    /**
     * 构造一个基于redis的批量分布式锁
     * redis锁的本质是在给定的时间范围内最早设置给定key对应的节点的值
     * 主要用于避免并发操作
     * 用于同时申请一批redis锁，每个key可以单独成功
     *
     * @param scope            锁所属的业务范围
     * @param keys             要申请的锁的名称列表，当且仅当所有的key都申请成功后此redis锁才获取成功
     * @param keyType          key类型描述
     * @param expiryTimeMillis 锁的存活期，单位毫秒，存活期需要长于500毫秒短于一个小时
     * @param <K>              key类型
     * @return 返回redis锁代理对象
     */
    public <K> RedisBatchLock<K> buildBatchLock(String scope, List<K> keys, RedisKeyType<K> keyType, long expiryTimeMillis) {
        return new RedisBatchLockImpl<>(redisPool, keyType, keys, expiryTimeMillis, scope);
    }
}

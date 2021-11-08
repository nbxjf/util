# Redis分布式锁

## 介绍

目前支持以下两种锁

1. RedisLock

   * 基础分布式锁，支持向redis提交一个key，获得一个分布式锁

   * 支持可重入，且会记录redis的重入次数，所以lock与unlock必须完整绑定出现，即使用必须完全符合规范

2. RedisBatchLock

   * 批量分布式锁，支持传入一批keys，获得一批分布式锁(允许每个key单独成功或者失败)，区别将多个value组成一个key获取一个分布式锁。

   * 主要用于解决循环时、或者一次申请多个redis锁时redis的压力



## 使用方法

1. 声明spring bean

   ```
   基于redis的分布式锁服务
   
   对象的声明过程如下：
   1. 在spring配置文件中增加如下bean定义
   
       <bean id="businessRedisConfig" class="redis.pool.RedisConfig">
           <property name="host" value="${business.redis.host}"/>
           <property name="port" value="${business.redis.port}"/>
           <property name="password" value="${business.redis.password}"/>
           <property name="database" value="${business.redis.database}"/>
       </bean>
       <bean id="businessRedisPool" class="redis.pool.RedisPool">
           <property name="config" ref="businessRedisConfig"/>
       </bean>
       <bean id="redisLockService" class="redis.lock.RedisLockService">
           <constructor-arg name="pool" ref="businessRedisPool"/>
       </bean>
   
   2. 在需要使用redis持久化服务的类中注入 redisLockService
   ```

   

2. 代码中使用，示例

   ```
       public void testLock_2() {
           RedisLock redisLock = redisLockService.buildLock("scope", "key", 300000);
           if (redisLock.tryAcquire()) {
               try {
                   // do something
               }catch (Exception e){
                   // exception handle
               }finally {
                   redisLock.release();
               }
           }
       }
   ```

   


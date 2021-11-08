# 基于Redis的持久化服务

## 介绍

基于redis封装的持久化服务，目的是为了：

* 支持使用对象作为key、value
  * 因为原生的redis数据结构，key和value必须是String类型，在使用的时候用户都需要自己转换，繁琐，扩展性差
* 对外隐藏使用方对Jedis的直接使用
* 强化使用方对于数据结构的规范

底层封装了Redis的基础数据结构，包含：

1. RedisMap，即string
2. RedisList，即list
3. RedisSet，即set
4. RedisSortedSet，即zset
5. RedisHash，即hash



## 使用方法

1. 声明Spring Bean

   ```
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
       <bean id="redisPersistentService" class="redis.persistent.RedisPersistentService">
           <constructor-arg name="pool" ref="businessRedisPool"/>
       </bean>
   
   2. 在需要使用redis持久化服务的类中注入 redisPersistentService
   ```

   


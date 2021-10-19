package redis.lock;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jeff_xu on 2021/10/15.
 * 基于redis的批处理分布式锁
 * 此接口用于向redis提交一批key，获得一批分布式锁(允许每个key单独成功或者失败)，区别将多个value组成一个key获取一个分布式锁
 * 主要用于解决循环时、或者一次申请多个redis锁时redis的压力
 *
 * @author Jeff_xu
 */
public interface RedisBatchLock<K> {
    /**
     * 尝试争用锁，返回是否上锁成功
     *
     * @return true表示至少成功获取了一个资源
     */
    boolean tryAcquire();

    /**
     * 释放锁，只释放被lock获取成功的资源
     */
    void release();

    /**
     * 获取 {@link #tryAcquire()}成功获取的资源
     *
     * @return 返回所有成功获取到的资源
     */
    List<K> getLockedKeys();

    /**
     * 获取 {@link #tryAcquire()}未成功获取的资源
     *
     * @return 返回所有成功获取到的资源
     */
    List<K> getUnLockedKeys();

    /**
     * 超时后不再上锁的资源列表。（包含至少释放过一次、或者未加锁成功的keys）
     * * 某些资源可能在等待的时候再次被其他进程获取，本函数不考虑这种情况。
     *
     * @param timeout  超时时间
     * @param timeUnit 超时时间的单位
     * @return 超时后不再上锁的资源列表
     */
    List<K> releasedKeys(long timeout, TimeUnit timeUnit);

}

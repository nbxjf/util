package redis.lock;

import java.util.concurrent.TimeUnit;

public interface RedisLock {

    /**
     * 获取资源，如果资源已被其他线程获取，则一直等待直到成功获取
     */
    void acquire() throws InterruptedException;

    /**
     * 尝试获取锁
     *
     * @return 返回true表示锁获取成功
     */
    boolean tryAcquire();

    /**
     * 尝试获取锁，直到设置的timeout时间，如果超出timeout还未获取锁，返回false
     *
     * @param timeout 超时时间
     * @param unit    超时时间的单位
     * @return 返回true表示锁获取成功
     */
    boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * 尝试释放锁
     * 只能在获取锁成功后调用此函数
     *
     * @return 返回false表示在释放锁时该锁已经过期
     * @throws IllegalStateException 试图释放一个没有被获取（或被成功获取）的锁会抛出此异常
     */
    boolean release() throws IllegalStateException;

}

package lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public interface RedisLock extends Lock {

    @Override
    void lock();

    @Override
    void lockInterruptibly() throws InterruptedException;

    @Override
    boolean tryLock();

    @Override
    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;

    @Override
    void unlock();

    @Override
    Condition newCondition();
}

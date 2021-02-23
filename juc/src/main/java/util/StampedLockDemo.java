package util;

import java.util.concurrent.locks.StampedLock;

/**
 * Created by Jeff_xu on 2020/10/21.
 *
 * @author Jeff_xu
 */
public class StampedLockDemo {

    public static void main(String[] args) {
        /* stampedLock 是对读写锁的优化，因为 ReadWriteLock在获取写锁的之前，必须要释放读锁，意味着在读的时候，不允许有写入的动作发生，这就是悲观读
         stampedLock提供了3种锁模
         1. 乐观读(乐观读，指的是在读锁的时候，也允许有写入操作)，乐观读实际上并没有加锁，只是维护了一个版本计数-stamp
         2. 悲观读 - 等同于 ReadWriteLock 的读锁
         3. 写锁 - 等同于 ReadWriteLock 的写锁
         */
        StampedLock stampedLock = new StampedLock();
    }

    public static void tryOptimisticRead(StampedLock stampedLock) {
        // 获取乐观读锁
        long version = stampedLock.tryOptimisticRead();

        // 校验乐观读锁是否发生改变
        if (!stampedLock.validate(version)) {
            // 获取一个悲观读锁
            version = stampedLock.readLock();
            try {
                // do some thing
            } finally {
                // 释放悲观读锁
                stampedLock.unlockRead(version);
            }
        }
    }
}

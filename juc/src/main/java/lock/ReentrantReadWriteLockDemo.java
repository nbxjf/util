package lock;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Jeff_xu on 2020/10/21.
 *
 * @author Jeff_xu
 */
public class ReentrantReadWriteLockDemo {

    public static void main(String[] args) {
        ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();

        System.out.println(reentrantReadWriteLock.isFair());

        reentrantReadWriteLock.readLock().lock();
    }

    // 读锁
    public static void tryReadLock(ReentrantReadWriteLock reentrantReadWriteLock) {
        reentrantReadWriteLock.readLock().lock();

        try {
            //do something
        } finally {
            reentrantReadWriteLock.readLock().unlock();
        }
    }

    // 写锁
    public static void tryWriteLock(ReentrantReadWriteLock reentrantReadWriteLock) {
        reentrantReadWriteLock.writeLock().lock();

        try {
            //do some thing
        } finally {
            reentrantReadWriteLock.readLock().unlock();
        }
    }

    // 写锁可以 降级为读锁
    public static void writeLock2ReadLock(ReentrantReadWriteLock reentrantReadWriteLock) {
        reentrantReadWriteLock.readLock().lock();
        if (true) {
            // 在获取写锁之前必须先释放所有的读锁
            // Must release read lock before acquiring write lock
            reentrantReadWriteLock.readLock().unlock();
            // 获取写锁
            reentrantReadWriteLock.writeLock().lock();
            try {
                // do some thing
                // 在释放写锁之前可以先获取读锁，释放写锁后，读锁依然没有被释放，就达到了锁降级的目的
                reentrantReadWriteLock.readLock().lock();
            } finally {
                reentrantReadWriteLock.writeLock().unlock(); // Unlock write, still hold read
            }
        }

        try {

        } finally {
            reentrantReadWriteLock.readLock().unlock();
        }
    }

}

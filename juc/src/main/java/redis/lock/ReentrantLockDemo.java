package redis.lock;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Jeff_xu on 2020/10/16.
 *
 * @author Jeff_xu
 */
public class ReentrantLockDemo {

    public static void main(String[] args) {

    }

    public void lockFair() {
        // ReentrantLock可以显式的通过构造函数声明为公平锁
        // 公平锁的实现是通过创建FairSync（实现了 AQS），再 acquire 锁的时候判断队列的第一个元素是否为当前线程，不是则 park，保证了获取锁的顺序性
        ReentrantLock fairLock = new ReentrantLock(true);
        fairLock.lock();
        try {

        } finally {
            fairLock.unlock();
        }
    }

    public void lockNonFair() {
        ReentrantLock fairLock = new ReentrantLock();
        fairLock.lock();
        try {

        } finally {
            fairLock.unlock();
        }
    }
}

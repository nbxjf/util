package redis.lock;

import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import redis.common.BaseTest;

/**
 * Created by Jeff_xu on 2021/9/22.
 *
 * @author Jeff_xu
 */
@Slf4j
public class RedisLockServiceTest extends BaseTest {

    private RedisLockService redisLockService;

    @Before
    public void setUp() {
        super.setUp();
        redisLockService = new RedisLockService(redisPool);
    }

    @Test
    public void testLock() {
        final RedisLock redisLock = redisLockService.buildLock("test", "ut-test", 300000);
        try {
            boolean lock1 = redisLock.tryAcquire();
            log.info("lock1, {}", lock1);
            System.out.println("lock1:" + lock1);
            boolean lock2 = redisLock.tryAcquire(15, TimeUnit.SECONDS);
            log.info("lock2, {}", lock2);
            System.out.println("lock2:" + lock2);
        } catch (Exception e) {
            log.error("lock failed", e);
            System.out.println(e);
        }
    }

    @Test
    public void testLock_2() {
        final RedisLock redisLock = redisLockService.buildLock("test", "ut-test", 300000);
        try {
            boolean lock1 = redisLock.tryAcquire();
            log.info("lock1, {}", lock1);
            System.out.println("lock1:" + lock1);
            boolean lock2 = redisLock.tryAcquire(15, TimeUnit.SECONDS);
            log.info("lock2, {}", lock2);
            System.out.println("lock2:" + lock2);
        } catch (Exception e) {
            log.error("lock failed", e);
            System.out.println(e);
        }
    }
}

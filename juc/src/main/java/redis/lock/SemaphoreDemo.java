package redis.lock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Created by Jeff_xu on 2020/10/22.
 *
 * @author Jeff_xu
 */
public class SemaphoreDemo {

    // 信号量可以做资源竞争控制，可以用来控制同时访问特定资源的线程数量，通过协调各个线程，以保证合理的使用资源。

    // 声明的 permits 控制最多并发的线程数，通过 acquire(1) 表示尝试获取1个许可，remaining代表剩余的许可数。

    private static final Semaphore semaphore = new Semaphore(10);

    public static void main(String[] args) {

        ExecutorService executorService = Executors.newFixedThreadPool(40);

        for (int i = 0; i < 40; i++) {
            int finalI = i;
            executorService.execute(() -> {
                try {
                    test(finalI);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();

    }

    public static void test(int i) throws InterruptedException {
        semaphore.acquire(1);
        System.out.println(i);
    }
}

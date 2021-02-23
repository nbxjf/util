package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Jeff_xu on 2020/10/23.
 * 生产者消费者模型(简单样例)
 *
 * @author Jeff_xu
 */
public class ConsumerProviderDemo {

    private static volatile List<Integer> integers = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {

        //    // 一个线程生产，一个线程消费
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<?> submit = executorService.submit(new Producer(integers));
        Future<?> submit1 = executorService.submit(new Consumer(integers));
        executorService.shutdown();

        Thread.sleep(1000L);

        executorService.shutdown();
    }

    public static class Producer implements Runnable {

        private volatile List<Integer> integers = new ArrayList<>();

        public Producer(List<Integer> integers) {
            this.integers = integers;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (integers) {
                        while (integers.size() > 5) {
                            integers.wait();
                        }
                        int i = new Random().nextInt();
                        integers.add(i);
                        System.out.println("生产了：" + i);
                        integers.notify();
                        Thread.sleep(500);

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class Consumer implements Runnable {
        private volatile List<Integer> integers = new ArrayList<>();

        public Consumer(List<Integer> integers) {
            this.integers = integers;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (integers) {
                        while (integers.size() <= 0) {
                            integers.wait();
                        }
                        int value = integers.remove(0);
                        System.out.println("消费了:" + value);
                        integers.notify();
                        Thread.sleep(500);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

package util;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Jeff_xu on 2020/10/22.
 *
 * @author Jeff_xu
 */
public class CountDownLatchDemo {

    public static void main(String[] args) {
        CountDownLatch begin = new CountDownLatch(1);
        CountDownLatch end = new CountDownLatch(2);

        for(int i=0; i<2; i++){
            Thread thread = new Thread(new Player(begin,end),String.valueOf(i));
            thread.start();
        }

        try{
            System.out.println("the race begin");
            begin.countDown();
            System.out.println("the race end");
            // end 阻塞等待，直到 end count 变成0时被唤醒再继续往下走

            end.await();
            System.out.println("end xxxxx");
        }catch(Exception e){
            e.printStackTrace();
        }

    }


   static class Player implements Runnable{

        private CountDownLatch begin;

        private CountDownLatch end;

        Player(CountDownLatch begin,CountDownLatch end){
            this.begin = begin;
            this.end = end;
        }

        public void run() {

            try {

                System.out.println(Thread.currentThread().getName() + " start !");;
                // begin 阻塞等待，直到 begin count 变成0时被唤醒再继续往下走
                begin.await();
                System.out.println(Thread.currentThread().getName() + " arrived !");

                end.countDown();//countDown() 并不是直接唤醒线程,当end.getCount()为0时线程会自动唤醒

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}

package lock;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Jeff_xu on 2020/10/22.
 *
 * @author Jeff_xu
 */
public class CyclicBarrierDemo {

    //和CountDownLatch的区别
    //1. CountDownLatch 允许一个或多个线程等待一些特定的操作完成，而这些操作是在其它的线程中进行的，也就是说会出现 等待的线程 和 被等的线程 这样分明的角色；
    //2. CountDownLatch 构造函数中有一个 count 参数，表示有多少个线程需要被等待，对这个变量的修改是在其它线程中调用 countDown 方法，每一个不同的线程调用一次 countDown 方法就表示有一个被等待的线程到达，count 变为 0 时，latch（门闩）就会被打开，处于等待状态的那些线程接着可以执行；
    //3. CountDownLatch 是一次性使用的，也就是说latch门闩只能只用一次，一旦latch门闩被打开就不能再次关闭，将会一直保持打开状态，因此 CountDownLatch 类也没有为 count 变量提供 set 的方法；

    public static void main(String[] args) {

        CyclicBarrier cyclicBarrier = new CyclicBarrier(8);
        List<Athlete> athleteList = new ArrayList<>();
        athleteList.add(new Athlete(cyclicBarrier, "博尔特"));
        athleteList.add(new Athlete(cyclicBarrier, "鲍威尔"));
        athleteList.add(new Athlete(cyclicBarrier, "盖伊"));
        athleteList.add(new Athlete(cyclicBarrier, "布雷克"));
        athleteList.add(new Athlete(cyclicBarrier, "加特林"));
        athleteList.add(new Athlete(cyclicBarrier, "苏炳添"));
        athleteList.add(new Athlete(cyclicBarrier, "路人甲"));
        athleteList.add(new Athlete(cyclicBarrier, "路人乙"));
        Executor executor = Executors.newFixedThreadPool(8);
        for (Athlete athlete : athleteList) {
            executor.execute(athlete);
        }
    }

    static class Athlete implements Runnable {

        private CyclicBarrier cyclicBarrier;
        private String name;

        public Athlete(CyclicBarrier cyclicBarrier, String name) {
            this.cyclicBarrier = cyclicBarrier;
            this.name = name;
        }

        @Override
        public void run() {
            System.out.println(name + "就位");
            try {
                cyclicBarrier.await();
                Random random = new Random();
                double time = random.nextDouble() + 9;
                System.out.println(name + ": " + time);
            } catch (Exception e) {
            }
        }
    }

}

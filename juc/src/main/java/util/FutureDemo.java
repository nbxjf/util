package util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by Jeff_xu on 2021/2/3.
 *
 * @author Jeff_xu
 */
public class FutureDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        int a = 123;
        FutureTask<Integer> futureTask = new FutureTask<>(() -> System.out.println("Runnable"), a);
        new Thread(futureTask).start();
        Integer integer = futureTask.get();
        System.out.println(a);
        System.out.println(integer);
    }
}

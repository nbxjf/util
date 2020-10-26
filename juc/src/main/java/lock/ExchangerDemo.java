package lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Jeff_xu on 2020/10/23.
 *
 * @author Jeff_xu
 */
public class ExchangerDemo {

    public static void main(String[] args) throws InterruptedException {
        Exchanger exchanger = new Exchanger();
        List<FruitExchanger> fruitExchangerList = new ArrayList<>();
        fruitExchangerList.add(new FruitExchanger(exchanger, "苹果"));
        fruitExchangerList.add(new FruitExchanger(exchanger, "李子"));
        fruitExchangerList.add(new FruitExchanger(exchanger, "芒果"));
        fruitExchangerList.add(new FruitExchanger(exchanger, "猕猴桃"));
        fruitExchangerList.add(new FruitExchanger(exchanger, "香蕉"));
        fruitExchangerList.add(new FruitExchanger(exchanger, "柚子"));
        fruitExchangerList.add(new FruitExchanger(exchanger, "龙眼"));

        ExecutorService ex = Executors.newFixedThreadPool(7);
        for (FruitExchanger fruitExchanger : fruitExchangerList) {
            ex.execute(fruitExchanger);
        }
        Thread.currentThread().join();
        System.out.println("交换结束");
        ex.shutdown();
    }

    static class FruitExchanger implements Runnable {

        private Exchanger exchanger;
        private String name;

        public FruitExchanger(Exchanger exchanger, String name) {
            this.exchanger = exchanger;
            this.name = name;
        }

        @Override
        public void run() {
            try {
                Object result = exchanger.exchange(name);
                System.out.println(String.format("交换%s 得到了 %s", name, result));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

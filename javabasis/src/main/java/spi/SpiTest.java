package spi;

import java.util.ServiceLoader;

/**
 * Created by Jeff_xu on 2021/1/6.
 *
 * @author Jeff_xu
 */
public class SpiTest {

    public static void main(String[] args) {
        /*
        Log4j: test log
        Logback: test log
         */
        ServiceLoader<Log> logs = ServiceLoader.load(Log.class);
        for (Log next : logs) {
            next.log("test log");
        }
    }
}

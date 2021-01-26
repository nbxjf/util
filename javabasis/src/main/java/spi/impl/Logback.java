package spi.impl;

import spi.Log;

/**
 * Created by Jeff_xu on 2021/1/6.
 *
 * @author Jeff_xu
 */
public class Logback implements Log {
    @Override
    public void log(String info) {
        System.out.println("Logback: " + info);
    }
}

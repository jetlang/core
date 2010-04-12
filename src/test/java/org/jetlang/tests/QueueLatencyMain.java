package org.jetlang.tests;

import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.ThreadFiber;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * User: mrettig
 * Date: Sep 28, 2009
 */
public class QueueLatencyMain {

    public static void main(String[] args) {
        Fiber f = new ThreadFiber();
        f.start();
        final long[] totalLatency = new long[1];

        final int total = 20000;
        for (int i = 0; i < total; i++) {
            Runnable toRun = new Runnable() {
                final long timestamp = System.nanoTime();

                public void run() {
                    long m = (System.nanoTime() - timestamp);
                    totalLatency[0] += m;
                    //System.out.println("m = " + m);
                }
            };
            f.execute(toRun);
            Thread.sleep(1);
        }
        final CountDownLatch latch = new CountDownLatch(1);
        Runnable shutdown = new Runnable() {
            public void run() {
                latch.countDown();
            }
        };
        long micros = TimeUnit.NANOSECONDS.toMicros(totalLatency[0]);
        System.out.println("totalLatency = " + micros + " avg: " + micros / total);
        f.dispose();
    }
}

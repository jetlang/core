package org.jetlang.examples;

/**
 * The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 * contributed by Klaus Friedel
 */

import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.PoolFiberFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadRingJetlang {
    static final int THREAD_COUNT = 503;

    public static class MessageThread {
        MessageThread nextThread;
        private int name;
        private Fiber fiber;
        private CountDownLatch done;

        public MessageThread(MessageThread nextThread, int name, Fiber fiber, CountDownLatch done) {
            this.nextThread = nextThread;
            this.name = name;
            this.fiber = fiber;
            this.done = done;
        }

        public void enqueue(final Integer hopsRemaining) {
            Runnable runnable = new Runnable() {
                public void run() {
                    if (hopsRemaining == 0) {
                        System.out.println(name);
                        done.countDown();
                        return;
                    }
                    int message = hopsRemaining - 1;
                    nextThread.enqueue(message);
                }
            };
            fiber.execute(runnable);
        }

        public void start() {
            fiber.start();
        }
    }

    public static void main(String args[]) throws Exception {
        long start = System.currentTimeMillis();
        //int hopCount = Integer.parseInt(args[0]);
        int hopCount = 10000000;
        ExecutorService pool = Executors.newFixedThreadPool(2);
        PoolFiberFactory fact = new PoolFiberFactory(pool);
        MessageThread first = null;
        MessageThread last = null;
        CountDownLatch done = new CountDownLatch(1);
        for (int i = THREAD_COUNT; i >= 1; i--) {
            first = new MessageThread(first, i, fact.create(), done);
            if (i == THREAD_COUNT) last = first;
        }
        // close the ring:
        last.nextThread = first;

        // start all Threads
        MessageThread t = first;
        do {
            t.start();
            t = t.nextThread;
        } while (t != first);
        // inject message
        first.enqueue(hopCount);
        done.await();
        System.out.println("time = " + (System.currentTimeMillis() - start));
        pool.shutdown();
    }
}
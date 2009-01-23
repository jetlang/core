package org.jetlang.examples;

/**
 * The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 * contributed by Klaus Friedel
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

public class ThreadRing {
    static final int THREAD_COUNT = 503;

    public static class MessageThread extends Thread {
        MessageThread nextThread;
        private CountDownLatch done;
        volatile Integer message;

        public MessageThread(MessageThread nextThread, int name, CountDownLatch done) {
            super("" + name);
            this.nextThread = nextThread;
            this.done = done;
        }

        public void run() {
            while (true) nextThread.enqueue(dequeue());
        }

        public void enqueue(Integer hopsRemaining) {
            if (hopsRemaining == 0) {
                System.out.println(getName());
                done.countDown();
                return;
            }
            // as only one message populates the ring, it's impossible
            // that queue is not empty
            message = hopsRemaining - 1;
            LockSupport.unpark(this); // work waiting...
        }

        private Integer dequeue() {
            while (message == null) {
                LockSupport.park();
            }
            Integer msg = message;
            message = null;
            return msg;
        }
    }

    public static void main(String args[]) throws Exception {
        long start = System.currentTimeMillis();
        //int hopCount = Integer.parseInt(args[0]);
        int hopCount = 10000000;
        CountDownLatch done = new CountDownLatch(1);
        MessageThread first = null;
        MessageThread last = null;
        for (int i = THREAD_COUNT; i >= 1; i--) {
            first = new MessageThread(first, i, done);
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
        System.out.println("done " + (System.currentTimeMillis() - start));
    }
}
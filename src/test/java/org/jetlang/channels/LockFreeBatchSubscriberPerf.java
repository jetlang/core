package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.MessageReader;
import org.jetlang.fibers.ThreadFiber;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LockFreeBatchSubscriberPerf {

    public static void main(String[] args) throws InterruptedException {
        ThreadFiber fiber = new ThreadFiber();
        fiber.start();
        final int total = 50000000;
        final CountDownLatch latch = new CountDownLatch(1);
        Callback<ConcurrentLinkedQueue<String>> cb = new Callback<ConcurrentLinkedQueue<String>>() {
            int count = 0;

            public void onMessage(ConcurrentLinkedQueue<String> message) {
                for (String val = message.poll(); val != null; val = message.poll()) {
                    count++;
                }
                if (count >= total) {
                    latch.countDown();
                }
            }
        };

        Callback<List<String>> listCb = new Callback<List<String>>() {
            int count = 0;

            public void onMessage(List<String> message) {
                for (int val = 0; val < message.size(); val++) {
                    String m = message.get(val);
                    count++;
                }
                if (count >= total) {
                    latch.countDown();
                    System.out.println("count = " + count);
                }
            }
        };

        Callback<MessageReader<String>> recyclingCb = new Callback<MessageReader<String>>() {
            int count = 0;

            public void onMessage(MessageReader<String> message) {
                for (int val = 0; val < message.size(); val++) {
                    String m = message.get(val);
                    count++;
                }
                if (count >= total) {
                    latch.countDown();
                    System.out.println("count = " + count);
                }
            }
        };
        Channel<String> c = new MemoryChannel<String>();
        LockFreeBatchSubscriber<String> sub = new LockFreeBatchSubscriber<String>(fiber, cb, 0, TimeUnit.MICROSECONDS);
        //BatchSubscriber<String> sub = new BatchSubscriber<String>(fiber, listCb, 0, TimeUnit.MICROSECONDS);
        //RecyclingBatchSubscriber<String> sub = new RecyclingBatchSubscriber<String>(fiber, recyclingCb, 0, TimeUnit.MICROSECONDS);

        c.subscribe(sub);

        long start = System.currentTimeMillis();
        for (int i = 0; i < total; i++) {
            c.publish("hello");
        }
        boolean b = latch.await(10, TimeUnit.SECONDS);
        long diff = System.currentTimeMillis() - start;
        System.out.println(sub.getClass().getSimpleName() + " Time: = " + diff + " " + b);
    }
}

package org.jetlang.examples;


import org.jetlang.channels.*;
import org.jetlang.core.Callback;
import org.jetlang.core.Filter;
import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.PoolFiberFactory;
import org.jetlang.fibers.ThreadFiber;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Some contrived examples that demonstrate the basic publish and
 * subscribe features in jetlang.
 */
public class BasicExamples {

    @Test
    public void basicPubSubWithThreadPool() throws InterruptedException {
        ExecutorService service = Executors.newCachedThreadPool();
        PoolFiberFactory fact = new PoolFiberFactory(service);
        Fiber fiber = fact.create();
        fiber.start();
        Channel<String> channel = new MemoryChannel<String>();

        final CountDownLatch reset = new CountDownLatch(1);
        Callback<String> runnable = new Callback<String>() {
            public void onMessage(String msg) {
                reset.countDown();
            }
        };
        channel.subscribe(fiber, runnable);
        channel.publish("hello");

        Assert.assertTrue(reset.await(5000, TimeUnit.MILLISECONDS));
        fiber.dispose();
        fact.dispose();
        service.shutdown();
    }

    @Test
    public void pubSubWithDedicatedThread() throws InterruptedException {
        Fiber fiber = new ThreadFiber();
        fiber.start();
        Channel<String> channel = new MemoryChannel<String>();

        final CountDownLatch reset = new CountDownLatch(1);
        Callback<String> runnable = new Callback<String>() {
            public void onMessage(String msg) {
                reset.countDown();
            }
        };

        channel.subscribe(fiber, runnable);
        channel.publish("hello");

        Assert.assertTrue(reset.await(5000, TimeUnit.MILLISECONDS));
        fiber.dispose();
    }

    @Test
    public void pubSubWithDedicatedThreadWithFilter() throws InterruptedException {
        Fiber fiber = new ThreadFiber();
        fiber.start();
        Channel<Integer> channel = new MemoryChannel<Integer>();

        final CountDownLatch reset = new CountDownLatch(1);
        Callback<Integer> onMsg = new Callback<Integer>() {
            public void onMessage(Integer x) {
                Assert.assertTrue(x % 2 == 0);
                if (x == 4) {
                    reset.countDown();
                }
            }
        };
        Filter<Integer> filter = new Filter<Integer>() {
            public boolean passes(Integer msg) {
                return msg % 2 == 0;
            }
        };
        ChannelSubscription<Integer> sub = new ChannelSubscription<Integer>(fiber, onMsg, filter);
        channel.subscribe(sub);
        channel.publish(1);
        channel.publish(2);
        channel.publish(3);
        channel.publish(4);

        Assert.assertTrue(reset.await(5000, TimeUnit.MILLISECONDS));
        fiber.dispose();
    }


    @Test
    public void batching() throws InterruptedException {
        Fiber fiber = new ThreadFiber();
        fiber.start();
        MemoryChannel<Integer> counter = new MemoryChannel<Integer>();
        final CountDownLatch reset = new CountDownLatch(1);
        Callback<List<Integer>> cb = new Callback<List<Integer>>() {
            int total = 0;

            public void onMessage(List<Integer> batch) {
                total += batch.size();
                if (total == 10) {
                    reset.countDown();
                }
            }
        };

        BatchSubscriber<Integer> batch = new BatchSubscriber<Integer>(fiber, cb, 0, TimeUnit.MILLISECONDS);
        counter.subscribe(batch);

        for (int i = 0; i < 10; i++) {
            counter.publish(i);
        }

        Assert.assertTrue(reset.await(10000, TimeUnit.MILLISECONDS));
        fiber.dispose();
    }

    @Test
    public void batchingWithKey() throws InterruptedException {
        Fiber fiber = new ThreadFiber();
        fiber.start();
        Channel<Integer> counter = new MemoryChannel<Integer>();
        final CountDownLatch reset = new CountDownLatch(1);
        Callback<Map<String, Integer>> cb = new Callback<Map<String, Integer>>() {
            public void onMessage(Map<String, Integer> batch) {
                if (batch.containsKey("9")) {
                    reset.countDown();
                }
            }
        };

        Converter<Integer, String> keyResolver = new Converter<Integer, String>() {
            public String convert(Integer msg) {
                return msg.toString();
            }
        };
        KeyedBatchSubscriber<String, Integer> batch = new KeyedBatchSubscriber<String, Integer>(fiber, cb, 0, TimeUnit.MILLISECONDS, keyResolver);
        counter.subscribe(batch);

        for (int i = 0; i < 10; i++) {
            counter.publish(i);
        }

        Assert.assertTrue(reset.await(10000, TimeUnit.MILLISECONDS));
        fiber.dispose();
    }

    @Test
    public void requestReplyWithBlockingQueue() throws InterruptedException {
        Fiber fiber = new ThreadFiber();
        fiber.start();
        BlockingQueue<String> replyQueue = new ArrayBlockingQueue<String>(1);
        MemoryChannel<BlockingQueue<String>> channel = new MemoryChannel<BlockingQueue<String>>();
        Callback<BlockingQueue<String>> replyCb = new Callback<BlockingQueue<String>>() {
            public void onMessage(BlockingQueue<String> message) {
                try {
                    message.put("hello");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        channel.subscribe(fiber, replyCb);
        channel.publish(replyQueue);

        Assert.assertEquals("hello", replyQueue.poll(10, TimeUnit.SECONDS));
        fiber.dispose();
    }

}
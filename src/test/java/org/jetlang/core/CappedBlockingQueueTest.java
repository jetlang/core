package org.jetlang.core;

import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.ThreadFiber;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CappedBlockingQueueTest {

    @Test
    public void executeCappedFiber() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            executeCappedProducerConsumer();
        }
    }

    private void executeCappedProducerConsumer() throws InterruptedException {
        final CountDownLatch consumedCountDown = new CountDownLatch(10);
        final CountDownLatch unblockConsumerLatch = new CountDownLatch(1);
        final AtomicInteger publishedCount = new AtomicInteger(0);
        final Fiber consumer = new ThreadFiber(new RunnableExecutorImpl(new BatchExecutorImpl(), new CappedBlockingQueue(1)), "thread", true);
        consumer.start();
        final Fiber producer = new ThreadFiber();
        producer.start();
        final Runnable consumerBlock = new Runnable() {
            public void run() {
                try {
                    unblockConsumerLatch.await();
                    consumedCountDown.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Runnable producerBlock = new Runnable() {
            public void run() {
                for (int i = 0; i < 10; i++) {
                    publishedCount.incrementAndGet();
                    consumer.execute(consumerBlock);
                }
            }
        };
        producer.execute(producerBlock);
        assertEquals(10, consumedCountDown.getCount());
        assertTrue("1 being processed, 1 in Q, 1 Blocked", publishedCount.get() <= 3);

        //unblock the slow consumer
        unblockConsumerLatch.countDown();
        assertTrue(consumedCountDown.await(10, TimeUnit.SECONDS));
        assertEquals(10, publishedCount.get());
        consumer.dispose();
        producer.dispose();

    }
}

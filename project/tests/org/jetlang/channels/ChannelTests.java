package org.jetlang.channels;

import org.jetlang.PerfTimer;
import org.jetlang.core.*;
import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.ThreadFiber;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * User: mrettig
 * Date: Jul 26, 2008
 * Time: 9:19:23 AM
 */
public class ChannelTests {

    @Test
    public void basicPubSubWithThreads() throws InterruptedException {

        //start receiver thread
        Fiber receiver = new ThreadFiber();
        receiver.start();

        final CountDownLatch latch = new CountDownLatch(1);

        Channel<String> channel = new Channel<String>();

        Callback<String> onMsg = new Callback<String>() {
            public void onMessage(String message) {
                latch.countDown();
            }
        };
        //add subscription for message on receiver thread
        channel.subscribe(receiver, onMsg);

        //publish message to receive thread. the publish method is thread safe.
        channel.publish("Hello");

        //wait for receiving thread to receive message
        latch.await(10, TimeUnit.SECONDS);

        //shutdown thread
        receiver.dispose();
    }


    @Test
    public void PubSub() {
        Channel<String> channel = new Channel<String>();
        SynchronousDisposingExecutor queue = new SynchronousDisposingExecutor();
        assertEquals(0, channel.publish("hello"));
        final List<String> received = new ArrayList<String>();
        Callback<String> onReceive = new Callback<String>() {
            public void onMessage(String data) {
                received.add(data);
            }
        };
        channel.subscribe(queue, onReceive);
        assertEquals(1, channel.publish("hello"));
        assertEquals(1, received.size());
        assertEquals("hello", received.get(0));

        channel.clearSubscribers();
        assertEquals(0, channel.publish("hello"));


    }

    @Test
    public void pubSubFilterTest() {
        Channel<Integer> channel = new Channel<Integer>();
        SynchronousDisposingExecutor execute = new SynchronousDisposingExecutor();
        final List<Integer> received = new ArrayList<Integer>();
        Callback<Integer> onReceive = new Callback<Integer>() {
            public void onMessage(Integer num) {
                received.add(num);
            }
        };
        ChannelSubscription<Integer> subber = new ChannelSubscription<Integer>(execute, onReceive);
        Filter<Integer> filter = new Filter<Integer>() {
            public boolean passes(Integer msg) {
                return msg % 2 == 0;
            }
        };
        subber.setFilterOnProducerThread(filter);
        channel.subscribeOnProducerThread(execute, subber);
        for (int i = 0; i <= 4; i++) {
            channel.publish(i);
        }
        assertEquals(3, received.size());
        assertEquals(0, received.get(0).intValue());
        assertEquals(2, received.get(1).intValue());
        assertEquals(4, received.get(2).intValue());

    }

    @Test
    public void pubSubUnsubscribe() {
        Channel<String> channel = new Channel<String>();
        SynchronousDisposingExecutor execute = new SynchronousDisposingExecutor();
        final boolean[] received = new boolean[1];
        Callback<String> onReceive = new Callback<String>() {
            public void onMessage(String message) {
                assertEquals("hello", message);
                received[0] = true;
            }
        };
        Disposable unsub = channel.subscribe(execute, onReceive);
        assertEquals(1, channel.publish("hello"));
        assertTrue(received[0]);
        unsub.dispose();
        assertEquals(0, channel.publish("hello"));
        unsub.dispose();
    }

    @Test
    public void SubToBatch() {
        Channel<String> channel = new Channel<String>();
        StubCommandContext execute = new StubCommandContext();
        final boolean[] received = new boolean[1];
        Callback<List<String>> onReceive = new Callback<List<String>>() {
            public void onMessage(List<String> data) {
                assertEquals(5, data.size());
                assertEquals("0", data.get(0));
                assertEquals("4", data.get(4));
                received[0] = true;
            }
        };

        BatchSubscriber<String> subscriber = new BatchSubscriber<String>(execute, onReceive, 10, TimeUnit.MILLISECONDS);
        channel.subscribe(subscriber);

        for (int i = 0; i < 5; i++) {
            channel.publish(i + "");
        }
        assertEquals(1, execute.Scheduled.size());
        execute.Scheduled.get(0).run();
        assertTrue(received[0]);
        execute.Scheduled.clear();
        received[0] = false;

        channel.publish("5");
        assertFalse(received[0]);
        assertEquals(1, execute.Scheduled.size());
    }

    @Test
    public void subToKeyedBatch() {
        Channel<Integer> channel = new Channel<Integer>();
        StubCommandContext execute = new StubCommandContext();
        final boolean[] received = new boolean[1];
        Callback<Map<String, Integer>> onReceive = new Callback<Map<String, Integer>>() {
            public void onMessage(Map<String, Integer> data) {
                assertEquals(2, data.keySet().size());
                assertEquals(data.get("0"), new Integer(0));
                received[0] = true;
            }
        };
        Converter<Integer, String> key = new Converter<Integer, String>() {
            public String Convert(Integer msg) {
                return msg.toString();
            }
        };
        KeyedBatchSubscriber<String, Integer> subscriber
                = new KeyedBatchSubscriber<String, Integer>(key, onReceive, execute, 0, TimeUnit.MILLISECONDS);
        channel.subscribe(subscriber);

        for (int i = 0; i < 5; i++) {
            channel.publish(i % 2);
        }

        assertEquals(1, execute.Scheduled.size());
        execute.Scheduled.get(0).run();
        assertTrue(received[0]);
        execute.Scheduled.clear();
        received[0] = false;
        channel.publish(999);
        assertFalse(received[0]);
        assertEquals(1, execute.Scheduled.size());
    }


    @Test
    public void SubscribeToLast() {
        Channel<Integer> channel = new Channel<Integer>();
        StubCommandContext execute = new StubCommandContext();
        final List<Integer> received = new ArrayList<Integer>();
        Callback<Integer> onReceive = new Callback<Integer>() {
            public void onMessage(Integer data)

            {
                received.add(data);
            }
        };
        LastSubscriber<Integer> lastSub = new LastSubscriber<Integer>(onReceive, execute, 3);
        channel.subscribe(lastSub);
        for (int i = 0; i < 5; i++) {
            channel.publish(i);
        }
        assertEquals(1, execute.Scheduled.size());
        assertEquals(0, received.size());
        execute.Scheduled.get(0).run();
        assertEquals(1, received.size());
        assertEquals(4, received.get(0).intValue());

        received.clear();
        execute.Scheduled.clear();
        channel.publish(5);
        assertEquals(1, execute.Scheduled.size());
        execute.Scheduled.get(0).run();
        assertEquals(5, received.get(0).intValue());
    }

    //

    @Test
    public void AsyncRequestReplyWithPrivateChannel() throws InterruptedException {
        Channel<Channel<String>> requestChannel = new Channel<Channel<String>>();
        Channel<String> replyChannel = new Channel<String>();
        Fiber responder = startFiber();
        Fiber receiver = startFiber();
        final CountDownLatch reset = new CountDownLatch(1);
        Callback<Channel<String>> onRequest = new Callback<Channel<String>>() {
            public void onMessage(Channel<String> message) {
                message.publish("hello");
            }
        };

        requestChannel.subscribe(responder, onRequest);
        Callback<String> onMsg = new Callback<String>() {
            public void onMessage(String message) {
                assertEquals("hello", message);
                reset.countDown();
            }
        };
        replyChannel.subscribe(receiver, onMsg);
        assertEquals(1, requestChannel.publish(replyChannel));
        assertTrue(reset.await(10, TimeUnit.SECONDS));
        responder.dispose();
        receiver.dispose();
    }

    @Test
    public void asyncRequestReplyWithBlockingQueue() throws InterruptedException {
        Channel<BlockingQueue<String>> requestChannel = new Channel<BlockingQueue<String>>();
        Fiber responder = startFiber();
        Callback<BlockingQueue<String>> onRequest = new Callback<BlockingQueue<String>>() {
            public void onMessage(BlockingQueue<String> message) {
                for (int i = 0; i < 5; i++)
                    message.add("hello" + i);
            }
        };

        requestChannel.subscribe(responder, onRequest);

        BlockingQueue<String> requestQueue = new ArrayBlockingQueue<String>(5);

        assertEquals(1, requestChannel.publish(requestQueue));
        for (int i = 0; i < 5; i++) {
            assertEquals("hello" + i, requestQueue.poll(30, TimeUnit.SECONDS));
        }
    }


    private int count = 0;

    private Fiber startFiber() {
        Fiber responder = new ThreadFiber(new RunnableExecutorImpl(), "thread" + (count++), true);
        responder.start();
        return responder;
    }

    @Test
    public void pointToPointPerfTest() throws InterruptedException {
        Channel<Integer> channel = new Channel<Integer>();
        RunnableExecutorImpl queue = new RunnableExecutorImpl();
        ThreadFiber bus = new ThreadFiber(queue, "testThread", true);
        bus.start();
        final Integer max = 5000000;
        final CountDownLatch reset = new CountDownLatch(1);
        Callback<Integer> onMsg = new Callback<Integer>() {
            public void onMessage(Integer count) {
                if (count.equals(max)) {
                    reset.countDown();
                }
            }
        };
        channel.subscribe(bus, onMsg);
        PerfTimer timer = new PerfTimer(max);
        try {
            for (int i = 0; i <= max; i++) {
                channel.publish(i);
                if (i % 50000 == 0) {
                    //Thread.sleep(1);
                }
            }
            boolean result = reset.await(30, TimeUnit.SECONDS);
            assertTrue(result);
        } finally {
            timer.dispose();
            bus.dispose();
        }
    }


}

class StubCommandContext implements Fiber {
    public List<Runnable> Scheduled = new ArrayList<Runnable>();

    public Disposable schedule(Runnable command, long delay, TimeUnit unit) {
        Scheduled.add(command);
        return null;
    }

    public Disposable scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        Scheduled.add(command);
        return null;
    }

    /// <summary>
    /// start consuming events.
    /// </summary>
    public void start() {
    }

    public void add(Disposable runOnStop) {
    }

    public boolean remove(Disposable disposable) {
        return false;
    }

    public int size() {
        return 0;
    }

    public void execute(Runnable command) {
        throw new RuntimeException("no impl");
    }

    public void dispose() {
    }
}

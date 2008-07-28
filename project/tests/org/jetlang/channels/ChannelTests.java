package org.jetlang.channels;

import org.jetlang.core.*;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * User: mrettig
 * Date: Jul 26, 2008
 * Time: 9:19:23 AM
 */
public class ChannelTests {
    @Test
    public void PubSub() {
        Channel<String> channel = new Channel<String>();
        SynchronousRunnableQueue queue = new SynchronousRunnableQueue();
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
        SynchronousRunnableQueue execute = new SynchronousRunnableQueue();
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
        SynchronousRunnableQueue execute = new SynchronousRunnableQueue();
        final boolean[] received = new boolean[1];
        Callback<String> onReceive = new Callback<String>() {
            public void onMessage(String message) {
                assertEquals("hello", message);
                received[0] = true;
            }
        };
        Stopable unsub = channel.subscribe(execute, onReceive);
        assertEquals(1, channel.publish("hello"));
        assertTrue(received[0]);
        unsub.stop();
        assertEquals(0, channel.publish("hello"));
        unsub.stop();
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

        ChannelBatchSubscriber<String> subscriber = new ChannelBatchSubscriber<String>(execute, onReceive, 10);
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
        final StubCommandContext execute = new StubCommandContext();
        final boolean[] received = new boolean[1];
        final Callback<Map<String, Integer>> onReceive = new Callback<Map<String, Integer>>() {
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
                = new KeyedBatchSubscriber<String, Integer>(key, onReceive, execute, 0);
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

//
//        [Test]
//        public void SubscribeToLast()
//        {
//            Channel<int> channel = new Channel<int>();
//            StubCommandContext execute = new StubCommandContext();
//            bool received = false;
//            int lastReceived = -1;
//            Action<int> onReceive = delegate(int data)
//                                        {
//                                            lastReceived = data;
//                                            received = true;
//                                        };
//            channel.SubscribeToLast(execute, onReceive, 0);
//
//            for (int i = 0; i < 5; i++)
//            {
//                channel.Publish(i);
//            }
//            Assert.AreEqual(1, execute.Scheduled.Count);
//            Assert.IsFalse(received);
//            Assert.AreEqual(-1, lastReceived);
//            execute.Scheduled[0]();
//            Assert.IsTrue(received);
//            Assert.AreEqual(4, lastReceived);
//            execute.Scheduled.Clear();
//            received = false;
//            lastReceived = -1;
//            channel.Publish(5);
//            Assert.IsFalse(received);
//            Assert.AreEqual(1, execute.Scheduled.Count);
//            execute.Scheduled[0]();
//            Assert.IsTrue(received);
//            Assert.AreEqual(5, lastReceived);
//        }

    //

    @Test
    public void AsyncRequestReplyWithPrivateChannel() throws InterruptedException {
        Channel<Channel<String>> requestChannel = new Channel<Channel<String>>();
        Channel<String> replyChannel = new Channel<String>();
        ProcessFiber responder = startFiber();
        ProcessFiber receiver = startFiber();
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
        responder.stop();
        receiver.stop();
    }

    private int count = 0;

    private ProcessFiber startFiber() {
        ProcessFiber responder = new ThreadFiber(new RunnableExecutorImpl(), "thread" + (count++), true);
        responder.start();
        return responder;
    }

    @Test
    public void PointToPointPerfTest() throws InterruptedException {
        Channel<Integer> channel = new Channel<Integer>();
        RunnableExecutorImpl queue = new RunnableExecutorImpl(new PerfCommandExecutor());
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
            timer.stop();
            bus.stop();
        }
    }
}

class StubCommandContext implements ProcessFiber {
    public List<Runnable> Scheduled = new ArrayList<Runnable>();

    public TimerControl schedule(Runnable command, long firstIntervalInMs) {
        Scheduled.add(command);
        return null;
    }

    public TimerControl scheduleOnInterval(Runnable command, long firstIntervalInMs, long regularIntervalInMs) {
        Scheduled.add(command);
        return null;
    }

    /// <summary>
    /// start consuming events.
    /// </summary>
    public void start() {
    }

    public void onStop(Stopable runOnStop) {
    }

    public void execute(Runnable command) {
        throw new RuntimeException("no impl");
    }

    public void stop() {
    }
}

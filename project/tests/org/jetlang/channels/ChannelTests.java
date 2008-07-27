package org.jetlang.channels;

import org.jetlang.core.*;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
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
        assertFalse(channel.publish("hello"));
        final List<String> received = new ArrayList<String>();
        Callback<String> onReceive = new Callback<String>() {
            public void onMessage(String data) {
                received.add(data);
            }
        };
        channel.subscribe(queue, onReceive);
        assertTrue(channel.publish("hello"));
        assertEquals(1, received.size());
        assertEquals("hello", received.get(0));

        channel.clearSubscribers();
        assertFalse(channel.publish("hello"));


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
        Unsubscriber unsub = channel.subscribe(execute, onReceive);
        assertTrue(channel.publish("hello"));
        assertTrue(received[0]);
        unsub.unsubscribe();
        assertFalse(channel.publish("hello"));
        unsub.unsubscribe();
    }
//
//        [Test]
//        public void SubToBatch()
//        {
//            Channel<string> channel = new Channel<string>();
//            StubCommandContext execute = new StubCommandContext();
//            bool received = false;
//            Action<IList<string>> onReceive = delegate(IList<string> data)
//                                                  {
//                                                      Assert.AreEqual(5, data.Count);
//                                                      Assert.AreEqual("0", data[0]);
//                                                      Assert.AreEqual("4", data[4]);
//                                                      received = true;
//                                                  };
//            channel.SubscribeToBatch(execute, onReceive, 0);
//
//            for (int i = 0; i < 5; i++)
//            {
//                channel.Publish(i.ToString());
//            }
//            Assert.AreEqual(1, execute.Scheduled.Count);
//            execute.Scheduled[0]();
//            Assert.IsTrue(received);
//            execute.Scheduled.Clear();
//            received = false;
//
//            channel.Publish("5");
//            Assert.IsFalse(received);
//            Assert.AreEqual(1, execute.Scheduled.Count);
//        }
//
//        [Test]
//        public void SubToKeyedBatch()
//        {
//            Channel<KeyValuePair<string, string>> channel = new Channel<KeyValuePair<string, string>>();
//            StubCommandContext execute = new StubCommandContext();
//            bool received = false;
//            Action<IDictionary<string, KeyValuePair<string, string>>> onReceive =
//                delegate(IDictionary<string, KeyValuePair<string, string>> data)
//                    {
//                        Assert.AreEqual(2, data.Keys.Count);
//                        Assert.AreEqual(data["0"], new KeyValuePair<string, string>("0", "4"));
//                        Assert.AreEqual(data["1"], new KeyValuePair<string, string>("1", "3"));
//                        received = true;
//                    };
//            Converter<KeyValuePair<string, string>, string> key =
//                delegate(KeyValuePair<string, string> pair) { return pair.Key; };
//            channel.SubscribeToKeyedBatch(execute, key, onReceive, 0);
//
//            for (int i = 0; i < 5; i++)
//            {
//                channel.Publish(new KeyValuePair<string, string>((i%2).ToString(), i.ToString()));
//            }
//            Assert.AreEqual(1, execute.Scheduled.Count);
//            execute.Scheduled[0]();
//            Assert.IsTrue(received);
//            execute.Scheduled.Clear();
//            received = false;
//
//            channel.Publish(new KeyValuePair<string, string>("1", "1"));
//            Assert.IsFalse(received);
//            Assert.AreEqual(1, execute.Scheduled.Count);
//        }
//
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
//        [Test]
//        public void AsyncRequestReplyWithPrivateChannel()
//        {
//            using (ProcessContextFactory factory = ProcessFactoryFixture.CreateAndStart())
//            {
//                Channel<Channel<string>> requestChannel = new Channel<Channel<string>>();
//                Channel<string> replyChannel = new Channel<string>();
//                IProcessBus responder = factory.CreatePooledAndStart();
//                IProcessBus receiver = factory.CreatePooledAndStart();
//                AutoResetEvent reset = new AutoResetEvent(false);
//                Action<Channel<string>> onRequest = delegate(Channel<string> reply) { reply.Publish("hello"); };
//                requestChannel.subscribe(responder, onRequest);
//                Action<string> onMsg = delegate(string msg)
//                                           {
//                                               Assert.AreEqual("hello", msg);
//                                               reset.Set();
//                                           };
//                replyChannel.subscribe(receiver, onMsg);
//                Assert.IsTrue(requestChannel.Publish(replyChannel));
//                Assert.IsTrue(reset.WaitOne(10000, false));
//            }
//        }
//
//        [Test]
//        public void AsyncRequestReplyWithPrivateChannelUsingThreads()
//        {
//            ProcessFiber responder = new ThreadFiber();
//            responder.start();
//            ProcessFiber receiver = new ThreadFiber();
//            receiver.start();
//
//            Channel<Channel<string>> requestChannel = new Channel<Channel<string>>();
//            Channel<string> replyChannel = new Channel<string>();
//            AutoResetEvent reset = new AutoResetEvent(false);
//            Action<Channel<string>> onRequest = delegate(Channel<string> reply) { reply.Publish("hello"); };
//            requestChannel.subscribe(responder, onRequest);
//            Action<string> onMsg = delegate(string msg)
//                                       {
//                                           Assert.AreEqual("hello", msg);
//                                           reset.Set();
//                                       };
//            replyChannel.subscribe(receiver, onMsg);
//            Assert.IsTrue(requestChannel.Publish(replyChannel));
//            Assert.IsTrue(reset.WaitOne(10000, false));
//
//            responder.Stop();
//            receiver.Stop();
//        }
//

    //

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

//        [Test]
//        public void BasicPubSubWithPoolQueue()
//        {
//            ProcessFiber execute = new PoolFiber();
//            execute.start();
//            Channel<string> hello = new Channel<string>();
//            Channel<string> hello2 = new Channel<string>();
//
//            AutoResetEvent reset = new AutoResetEvent(false);
//            Action<string> receiveHello = delegate(string str)
//                                              {
//                                                  Assert.AreEqual("hello", str);
//                                                  reset.Set();
//                                              };
//            hello.subscribe(execute, receiveHello);
//            hello2.subscribe(execute, receiveHello);
//            Assert.IsTrue(hello.Publish("hello"));
//            Assert.IsTrue(reset.WaitOne(10000, false));
//            execute.Stop();
//        }
}

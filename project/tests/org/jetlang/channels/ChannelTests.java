package org.jetlang.channels;

import org.jetlang.core.*;
import org.junit.Assert;
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
public class ChannelTests extends Assert {
    @Test
    public void PubSub() {
        Channel<String> channel = new Channel<String>();
        SynchronousCommandQueue queue = new SynchronousCommandQueue();
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

//        [Test]
//        public void PubSubFilterTest()
//        {
//            Channel<Integer> channel = new Channel<Integer>();
//            SynchronousCommandQueue queue = new SynchronousCommandQueue();
//            int received = 0;
//            Callback<int> onReceive = delegate(int data)
//                                        {
//                                            Assert.IsTrue(data%2 == 0);
//                                            received++;
//                                        };
//            ChannelSubscription<int> subber = new ChannelSubscription<int>(queue, onReceive);
//            subber.FilterOnProducerThread = delegate(int msg) { return msg%2 == 0; };
//            channel.SubscribeOnProducerThreads(subber);
//            for (int i = 0; i <= 4; i++)
//            {
//                channel.Publish(i);
//            }
//            Assert.AreEqual(3, received);
//        }
//
//
//        [Test]
//        public void PubSubUnsubscribe()
//        {
//            Channel<string> channel = new Channel<string>();
//            SynchronousCommandQueue queue = new SynchronousCommandQueue();
//            bool received = false;
//            Action<string> onReceive = delegate(string data)
//                                           {
//                                               Assert.AreEqual("hello", data);
//                                               received = true;
//                                           };
//            IUnsubscriber unsub = channel.subscribe(queue, onReceive);
//            Assert.IsTrue(channel.Publish("hello"));
//            Assert.IsTrue(received);
//            unsub.unsubscribe();
//            Assert.IsFalse(channel.Publish("hello"));
//            unsub.unsubscribe();
//        }
//
//        [Test]
//        public void SubToBatch()
//        {
//            Channel<string> channel = new Channel<string>();
//            StubCommandContext queue = new StubCommandContext();
//            bool received = false;
//            Action<IList<string>> onReceive = delegate(IList<string> data)
//                                                  {
//                                                      Assert.AreEqual(5, data.Count);
//                                                      Assert.AreEqual("0", data[0]);
//                                                      Assert.AreEqual("4", data[4]);
//                                                      received = true;
//                                                  };
//            channel.SubscribeToBatch(queue, onReceive, 0);
//
//            for (int i = 0; i < 5; i++)
//            {
//                channel.Publish(i.ToString());
//            }
//            Assert.AreEqual(1, queue.Scheduled.Count);
//            queue.Scheduled[0]();
//            Assert.IsTrue(received);
//            queue.Scheduled.Clear();
//            received = false;
//
//            channel.Publish("5");
//            Assert.IsFalse(received);
//            Assert.AreEqual(1, queue.Scheduled.Count);
//        }
//
//        [Test]
//        public void SubToKeyedBatch()
//        {
//            Channel<KeyValuePair<string, string>> channel = new Channel<KeyValuePair<string, string>>();
//            StubCommandContext queue = new StubCommandContext();
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
//            channel.SubscribeToKeyedBatch(queue, key, onReceive, 0);
//
//            for (int i = 0; i < 5; i++)
//            {
//                channel.Publish(new KeyValuePair<string, string>((i%2).ToString(), i.ToString()));
//            }
//            Assert.AreEqual(1, queue.Scheduled.Count);
//            queue.Scheduled[0]();
//            Assert.IsTrue(received);
//            queue.Scheduled.Clear();
//            received = false;
//
//            channel.Publish(new KeyValuePair<string, string>("1", "1"));
//            Assert.IsFalse(received);
//            Assert.AreEqual(1, queue.Scheduled.Count);
//        }
//
//
//        [Test]
//        public void SubscribeToLast()
//        {
//            Channel<int> channel = new Channel<int>();
//            StubCommandContext queue = new StubCommandContext();
//            bool received = false;
//            int lastReceived = -1;
//            Action<int> onReceive = delegate(int data)
//                                        {
//                                            lastReceived = data;
//                                            received = true;
//                                        };
//            channel.SubscribeToLast(queue, onReceive, 0);
//
//            for (int i = 0; i < 5; i++)
//            {
//                channel.Publish(i);
//            }
//            Assert.AreEqual(1, queue.Scheduled.Count);
//            Assert.IsFalse(received);
//            Assert.AreEqual(-1, lastReceived);
//            queue.Scheduled[0]();
//            Assert.IsTrue(received);
//            Assert.AreEqual(4, lastReceived);
//            queue.Scheduled.Clear();
//            received = false;
//            lastReceived = -1;
//            channel.Publish(5);
//            Assert.IsFalse(received);
//            Assert.AreEqual(1, queue.Scheduled.Count);
//            queue.Scheduled[0]();
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
//            ProcessFiber queue = new PoolFiber();
//            queue.start();
//            Channel<string> hello = new Channel<string>();
//            Channel<string> hello2 = new Channel<string>();
//
//            AutoResetEvent reset = new AutoResetEvent(false);
//            Action<string> receiveHello = delegate(string str)
//                                              {
//                                                  Assert.AreEqual("hello", str);
//                                                  reset.Set();
//                                              };
//            hello.subscribe(queue, receiveHello);
//            hello2.subscribe(queue, receiveHello);
//            Assert.IsTrue(hello.Publish("hello"));
//            Assert.IsTrue(reset.WaitOne(10000, false));
//            queue.Stop();
//        }
}

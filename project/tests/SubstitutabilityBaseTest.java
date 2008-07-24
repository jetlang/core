import com.jetlang.channels.Channel;
import com.jetlang.core.Callback;
import com.jetlang.core.IProcessQueue;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class SubstitutabilityBaseTest extends Assert {
    public abstract IProcessQueue CreateBus();

    public abstract void DoSetup();

    public abstract void DoTearDown();

    protected IProcessQueue _bus;

    @Before
    public void Setup() {
        DoSetup();
        _bus = CreateBus();
    }

    @After
    public void TearDown() {
        if (_bus != null) {
            _bus.Stop();
        }
        DoTearDown();
    }

//        @Test
//        public void ScheduleBeforeStart()
//        {
//            CountDownLatch reset = new CountDownLatch(1);
//
//            Ru onReset = delegate { reset.Set(); };
//            _bus.Schedule(onReset, 1);
//            _bus.Start();
//
//            Assert.IsTrue(reset.WaitOne(5000, false));
//        }

    @Test
    public void DoubleStartResultsInException() {
        _bus.Start();
        try {
            _bus.Start();
            Assert.fail("Should not Start");
        }
        catch (Exception e) {
        }
    }

    @Test
    public void PubSub() throws InterruptedException {
        _bus.Start();
        Channel<String> channel = new Channel<String>();
        Assert.assertFalse(channel.publish("hello"));
        final List<String> received = new ArrayList<String>();
        final CountDownLatch reset = new CountDownLatch(1);
        Callback<String> onReceive = new Callback<String>() {
            public void onMessage(String data) {
                received.add(data);
                reset.countDown();
            }
        };
        channel.subscribe(_bus, onReceive);
        assertTrue(channel.publish("hello"));
        assertTrue(reset.await(10, TimeUnit.SECONDS));
        assertEquals(1, received.size());
        assertEquals("hello", received.get(0));

        channel.clearSubscribers();
        assertFalse(channel.publish("hello"));
    }

//        @Test
//        public void AsyncRequestTimeout()
//        {
//            CountDownLatch reset = new CountDownLatch(1);
//            Command onTimeout = delegate { reset.Set(); };
//            _bus.Start();
//            OnMessage<string> reply = delegate { Assert.Fail("Should not be called"); };
//            _bus.SendAsyncRequest(new object(), "msg", reply, onTimeout, 1);
//            Assert.IsTrue(reset.WaitOne(5000, false));
//        }

//        [Test]
//        public void AsyncRequestWithReply()
//        {
//            IProcessBus replyBus = CreateBus(_contextFactory);
//            replyBus.Start();
//            string requestTopic = "request";
//            OnMessage<string> onMsg =
//                delegate(IMessageHeader header, string msg) { replyBus.Publish(header.ReplyTo, msg); };
//            replyBus.Subscribe(new TopicEquals(requestTopic), onMsg);
//            Command onTimeout = delegate { Assert.Fail("Should not timeout"); };
//            _bus.Start();
//            ManualResetEvent reset = new ManualResetEvent(false);
//            OnMessage<string> reply = delegate { reset.Set(); };
//            _bus.SendAsyncRequest("request", "msg", reply, onTimeout, 100);
//            Assert.IsTrue(reset.WaitOne(5000, false));
//            replyBus.Stop();
//        }

}
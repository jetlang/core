package org.jetlang.tests;

import junit.framework.Assert;
import org.jetlang.channels.MemoryChannel;
import org.jetlang.core.Callback;
import org.jetlang.core.Disposable;
import org.jetlang.fibers.Fiber;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class FiberBaseTest extends Assert {
    public abstract Fiber createFiber();

    public abstract void doSetup();

    public abstract void doTearDown();

    protected Fiber _bus;

    @Before
    public void Setup() {
        doSetup();
        _bus = createFiber();
    }

    @After
    public void TearDown() {
        if (_bus != null) {
            _bus.dispose();
            assertEquals(0, _bus.size());
        }
        doTearDown();
    }

    @Test
    public void ScheduleBeforeStart() throws InterruptedException {
        final CountDownLatch reset = new CountDownLatch(1);

        Runnable onReset = new Runnable() {
            public void run() {
                reset.countDown();
            }
        };
        _bus.schedule(onReset, 1, TimeUnit.MILLISECONDS);
        _bus.start();

        assertTrue(reset.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void ScheduleAndCancelBeforeStart() throws InterruptedException {
        final CountDownLatch reset = new CountDownLatch(1);
        final AtomicBoolean executed = new AtomicBoolean();
        Runnable toCancel = new Runnable() {
            public void run() {
                executed.set(true);
            }
        };
        Disposable control = _bus.schedule(toCancel, 0, TimeUnit.MILLISECONDS);
        Runnable toRun = new Runnable() {
            public void run() {
                reset.countDown();
            }
        };
        _bus.schedule(toRun, 0, TimeUnit.MILLISECONDS);
        control.dispose();
        _bus.start();
        assertTrue(reset.await(10, TimeUnit.SECONDS));
        assertFalse(executed.get());
    }

    @Test
    public void ScheduleOne() throws InterruptedException {
        final CountDownLatch reset = new CountDownLatch(1);
        _bus.start();
        Runnable onReset = new Runnable() {
            public void run() {
                reset.countDown();
            }
        };
        _bus.schedule(onReset, 1, TimeUnit.MILLISECONDS);
        assertTrue(reset.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void ScheduleInterval() throws InterruptedException {
        final CountDownLatch reset = new CountDownLatch(5);
        _bus.start();
        Runnable onReset = new Runnable() {
            public void run() {
                reset.countDown();
            }
        };
        _bus.scheduleAtFixedRate(onReset, 15, 15, TimeUnit.MILLISECONDS);
        assertTrue(reset.await(10, TimeUnit.SECONDS));
    }


    @Test
    public void testDoubleStartResultsInException() {
        _bus.start();
        try {
            _bus.start();
            Assert.fail("Should not start");
        } catch (Exception e) {
        }
    }

    @Test
    public void PubSub() throws InterruptedException {
        _bus.start();
        MemoryChannel<String> channel = new MemoryChannel<String>();

        channel.publish("hello");
        final List<String> received = new ArrayList<String>();
        final CountDownLatch reset = new CountDownLatch(1);
        Callback<String> onReceive = new Callback<String>() {
            public void onMessage(String data) {
                received.add(data);
                reset.countDown();
            }
        };
        channel.subscribe(_bus, onReceive);
        assertEquals(1, channel.subscriberCount());
        channel.publish("hello");
        assertTrue(reset.await(10, TimeUnit.SECONDS));
        assertEquals(1, received.size());
        assertEquals("hello", received.get(0));

        channel.clearSubscribers();
        channel.publish("hello");
    }

    @Test
    public void UnsubOnStop() throws InterruptedException {
        _bus.start();
        MemoryChannel<String> channel = new MemoryChannel<String>();
        Callback<String> onReceive = new Callback<String>() {
            public void onMessage(String data) {
            }
        };
        channel.subscribe(_bus, onReceive);
        assertEquals(1, channel.subscriberCount());
        _bus.dispose();
        assertEquals(0, channel.subscriberCount());
    }

    @Test
    public void Unsub() throws InterruptedException {
        _bus.start();
        MemoryChannel<String> channel = new MemoryChannel<String>();
        Callback<String> onReceive = new Callback<String>() {
            public void onMessage(String data) {
            }
        };
        Disposable unsub = channel.subscribe(_bus, onReceive);
        assertEquals(1, channel.subscriberCount());
        unsub.dispose();
        assertEquals(0, channel.subscriberCount());
    }

}
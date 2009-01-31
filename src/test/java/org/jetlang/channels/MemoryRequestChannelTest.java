package org.jetlang.channels;

import static junit.framework.Assert.assertEquals;
import org.jetlang.core.Callback;
import org.jetlang.core.Disposable;
import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.PoolFiberFactory;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * User: mrettig
 * Date: Jan 31, 2009
 * Time: 10:58:18 AM
 */
public class MemoryRequestChannelTest {
    private static ExecutorService pool;
    private static PoolFiberFactory fiberPool;

    private List<Fiber> active = new ArrayList<Fiber>();

    @BeforeClass
    public static void createPool() {
        pool = Executors.newCachedThreadPool();
        fiberPool = new PoolFiberFactory(pool);
    }

    @AfterClass
    public static void destroyPool() {
        if (pool != null) {
            pool.shutdownNow();
        }
        if (fiberPool != null) {
            fiberPool.dispose();
        }
    }

    @After
    public void stopAll() {
        for (Fiber fiber : active) {
            fiber.dispose();
        }
        active.clear();
    }

    @Test
    public void simpleRequestResponse() throws InterruptedException {
        Fiber req = startFiber();
        Fiber reply = startFiber();
        MemoryRequestChannel<String, Integer> channel = new MemoryRequestChannel<String, Integer>();
        Callback<Request<String, Integer>> onReq = new Callback<Request<String, Integer>>() {
            public void onMessage(Request<String, Integer> message) {
                message.reply(1);
            }
        };
        channel.subscribe(reply, onReq);

        final CountDownLatch done = new CountDownLatch(1);
        Callback<Integer> onReply = new Callback<Integer>() {
            public void onMessage(Integer message) {
                assertEquals(1, message.intValue());
                done.countDown();
            }
        };
        channel.publish(req, "hello", onReply);
        assertTrue(done.await(10, TimeUnit.SECONDS));

    }

    @Test
    public void simpleRequestResponseWithEndSession() throws InterruptedException {
        Fiber req = startFiber();
        Fiber reply = startFiber();
        MemoryRequestChannel<String, Integer> channel = new MemoryRequestChannel<String, Integer>();
        final CountDownLatch done = new CountDownLatch(1);
        Callback<Request<String, Integer>> onReq = new Callback<Request<String, Integer>>() {
            public void onMessage(Request<String, Integer> message) {
                message.reply(1);
            }
        };
        Callback<Request<String, Integer>> onEnd = new Callback<Request<String, Integer>>() {
            public void onMessage(Request<String, Integer> message) {
                done.countDown();
            }
        };
        channel.subscribe(reply, onReq, onEnd);

        final CountDownLatch rcv = new CountDownLatch(1);
        Callback<Integer> onReply = new Callback<Integer>() {
            public void onMessage(Integer message) {
                assertEquals(1, message.intValue());
                rcv.countDown();
            }
        };
        Disposable reqController = channel.publish(req, "hello", onReply);
        assertTrue(rcv.await(10, TimeUnit.SECONDS));
        reqController.dispose();
        assertTrue(done.await(10, TimeUnit.SECONDS));

    }

    private Fiber startFiber() {
        Fiber f = fiberPool.create();
        active.add(f);
        f.start();
        return f;
    }
}

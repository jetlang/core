package org.jetlang.examples.pingpong;

import junit.framework.Assert;
import org.jetlang.core.Disposable;
import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.PoolFiberFactory;
import org.jetlang.fibers.ThreadFiber;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * User: mrettig
 * Date: Dec 7, 2008
 * Time: 12:50:58 PM
 */
public class PingPongTest {

    @Test
    public void executWithDedicatedThreads() throws Exception {
        PingPongChannels channels = new PingPongChannels();

        ThreadFiber pingThread = new ThreadFiber();
        Ping ping = new Ping(channels, pingThread, 100000);

        ThreadFiber pongThread = new ThreadFiber();
        Pong pong = new Pong(channels, pongThread);

        pong.start();
        ping.start();

        //wait for threads to cleanly exit
        pingThread.join();
        pongThread.join();
    }

    @Test
    public void executeWithPool() throws InterruptedException {
        ExecutorService exec = Executors.newCachedThreadPool();
        PoolFiberFactory fact = new PoolFiberFactory(exec);
        PingPongChannels channels = new PingPongChannels();

        final CountDownLatch onstop = new CountDownLatch(2);
        Disposable dispose = new Disposable() {
            public void dispose() {
                onstop.countDown();
            }
        };
        Fiber pingThread = fact.create();
        pingThread.add(dispose);
        Ping ping = new Ping(channels, pingThread, 100000);

        Fiber pongThread = fact.create();
        pongThread.add(dispose);
        Pong pong = new Pong(channels, pongThread);

        pong.start();
        ping.start();

        //wait for fibers to be disposed.
        Assert.assertTrue(onstop.await(60, TimeUnit.SECONDS));
        //destroy thread pool
        exec.shutdown();
    }
}

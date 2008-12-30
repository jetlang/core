package org.jetlang.examples.download;


import org.jetlang.core.Disposable;
import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.PoolFiberFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public final static int NUM_ACTORS = 3;

    public static void main(String[] args) {
        ExecutorService exec = Executors.newCachedThreadPool();
        PoolFiberFactory factory = new PoolFiberFactory(exec);
        Channels channels = new Channels();

        // when the poison pill is received, the fiber.dispose() call will
        // call this and decrement the countdown latch. The onstop.await()
        // will block until the latch is zero, so that way the manager waits
        // for all the actors to complete before exiting
        final CountDownLatch onstop = new CountDownLatch(NUM_ACTORS);
        Disposable dispose = new Disposable() {
            public void dispose() {
                onstop.countDown();
            }
        };

        Fiber downloadFiber = factory.create();
        downloadFiber.add(dispose);
        DownloadActor downloadActor =
                new DownloadActor(channels.downloadChannel, channels.indexChannel,
                        channels.downloadStopChannel, channels.indexStopChannel,
                        downloadFiber);

        Fiber indexFiber = factory.create();
        indexFiber.add(dispose);
        IndexActor indexActor =
                new IndexActor(channels.indexChannel, channels.writeChannel,
                        channels.indexStopChannel, channels.writeStopChannel,
                        indexFiber);

        Fiber writeFiber = factory.create();
        writeFiber.add(dispose);
        WriteActor writeActor =
                new WriteActor(channels.writeChannel, channels.writeStopChannel,
                        writeFiber);

        downloadActor.start();
        indexActor.start();
        writeActor.start();


        long nanoStart = System.nanoTime();
        // seed the incoming channel with 10,000 requests
        for (int i = 0; i < 100000; i++) {
            String payload = "Requested " + i;
            Log(payload);
            channels.downloadChannel.publish(payload);
        }
        // send the poison pill to stop processing
        channels.downloadStopChannel.publish(null);

        try {
            onstop.await();
            long elapsed = System.nanoTime() - nanoStart;
            System.out.println("elapsed = " + TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        exec.shutdown();
    }

    static void Log(String msg) {
        //System.out.println(msg);
    }
}
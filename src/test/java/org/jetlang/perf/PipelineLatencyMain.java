package org.jetlang.perf;

import org.jetlang.channels.MemoryChannel;
import org.jetlang.core.Callback;
import org.jetlang.fibers.ThreadFiber;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * User: mrettig
 * Date: Jul 19, 2010
 * Time: 4:14:39 PM
 */
public class PipelineLatencyMain {

    public static void main(String[] args) throws InterruptedException {
        int channelCount = 5;
        MemoryChannel<Msg>[] channels = new MemoryChannel[channelCount];
        for (int i = 0; i < channels.length; i++) {
            channels[i] = new MemoryChannel<Msg>();
        }

        final ThreadFiber[] fibers = new ThreadFiber[channelCount];
        for (int i = 0; i < fibers.length; i++) {
            fibers[i] = new ThreadFiber();
            fibers[i].start();
            final int prior = i - 1;
            final boolean isLast = i + 1 == fibers.length;
            final MemoryChannel<Msg> target = !isLast ? channels[i] : null;
            if (prior >= 0) {
                Callback<Msg> cb = new Callback<Msg>() {
                    public void onMessage(Msg message) {
                        if (target != null)
                            target.publish(message);
                        else {
                            long now = System.nanoTime();
                            long diff = now - message.time;
                            if (message.log)
                                System.out.println("diff = " + TimeUnit.NANOSECONDS.toMicros(diff));
                            message.latch.countDown();
                        }
                    }
                };
                channels[prior].subscribe(fibers[i], cb);
            }
        }

        for (int i = 0; i < 10000; i++) {
            Msg s = new Msg(i == 9999);
            channels[0].publish(s);
            s.latch.await();
            //System.out.println("s = " + s.latch.await(10, TimeUnit.SECONDS));
        }

        for (int i = 0; i < 5; i++) {
            Msg s = new Msg(true);
            channels[0].publish(s);
            System.out.println("s = " + s.latch.await(10, TimeUnit.SECONDS));
            Thread.sleep(10);
        }

        for (ThreadFiber fiber : fibers) {
            fiber.dispose();
        }
    }

    private static class Msg {
        public final boolean log;

        public Msg(boolean log) {
            this.log = log;
        }

        public final Long time = System.nanoTime();
        public final CountDownLatch latch = new CountDownLatch(1);
    }
}

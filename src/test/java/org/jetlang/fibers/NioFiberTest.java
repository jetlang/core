package org.jetlang.fibers;

import org.jetlang.tests.FiberBaseTest;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NioFiberTest extends FiberBaseTest {

    @Test
    public void singleEvent() throws IOException, InterruptedException {
        NioFiber fiber = new NioFiberImpl();
        fiber.start();
        final CountDownLatch latch = new CountDownLatch(3);
        fiber.execute(new Runnable() {
            public void run() {
                latch.countDown();
            }
        });

        fiber.execute(new Runnable() {
            public void run() {
                latch.countDown();
            }
        });

        fiber.execute(new Runnable() {
            public void run() {
                latch.countDown();
            }
        });

        final boolean await = latch.await(30, TimeUnit.SECONDS);
        assertTrue(await);
        fiber.dispose();
    }


    @Test
    public void eventBurstFromDifferentThreads() throws IOException, InterruptedException {
        final NioFiber fiber = new NioFiberImpl();
        fiber.start();
        int total = 5000000;
        final CountDownLatch latch = new CountDownLatch(total);
        final ExecutorService executorService = Executors.newFixedThreadPool(4);
        for (int i = 0; i < total; i++) {
            executorService.execute(new Runnable() {
                public void run() {
                    fiber.execute(new Runnable() {
                        public void run() {
                            latch.countDown();
                        }
                    });
                }
            });
        }
        final boolean await = latch.await(30, TimeUnit.SECONDS);
        assertTrue(await);
        fiber.dispose();
    }

    @Test
    public void schedule() throws InterruptedException {
        NioFiber fiber = new NioFiberImpl();
        fiber.start();
        final CountDownLatch latch = new CountDownLatch(3);
        fiber.scheduleAtFixedRate(new Runnable() {
            public void run() {
                latch.countDown();
            }
        }, 1, 10, TimeUnit.MILLISECONDS);
        final boolean await = latch.await(30, TimeUnit.SECONDS);
        assertTrue(await);
        fiber.dispose();
    }

    @Test
    public void pipeData() throws IOException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(10);
        NioChannelHandler.PipeReader pipePing = new NioChannelHandler.PipeReader() {
            int count = 1;
            private ByteBuffer buffer = ByteBuffer.allocate(4);

            @Override
            protected void onData(Pipe.SourceChannel source) {
                try {
                    source.read(buffer);
                    if (buffer.position() == 4) {
                        buffer.flip();
                        assertEquals(count, buffer.getInt());
                        count++;
                        buffer.clear();
                        latch.countDown();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        NioFiber fiber = new NioFiberImpl();
        fiber.addHandler(pipePing);
        fiber.start();

        final Pipe.SinkChannel sink = pipePing.getSink();
        for (int i = 1; i < 11; i++) {
            ByteBuffer b = ByteBuffer.allocateDirect(4);
            b.putInt(i);
            b.flip();
            while (b.hasRemaining()) {
                sink.write(b);
            }
        }

        final boolean await = latch.await(10, TimeUnit.SECONDS);
        assertTrue(await);
        fiber.dispose();
    }

    @Override
    public Fiber createFiber() {
        return new NioFiberImpl();
    }

    @Override
    public void doSetup() {

    }

    @Override
    public void doTearDown() {

    }
}

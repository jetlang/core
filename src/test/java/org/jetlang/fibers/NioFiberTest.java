package org.jetlang.fibers;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class NioFiberTest {

    @Test
    public void singleEvent() throws IOException, InterruptedException {
        NioFiber fiber = new NioFiberImpl();
        fiber.start();
        final CountDownLatch latch = new CountDownLatch(3);
        fiber.execute(new Runnable() {
            @Override
            public void run() {
                latch.countDown();
            }
        });

        fiber.execute(new Runnable() {
            @Override
            public void run() {
                latch.countDown();
            }
        });

        fiber.execute(new Runnable() {
            @Override
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
        final NioFiber fiber = new NioFiberImpl(new NioBatchExecutorImpl(), new ArrayList<NioChannelHandler>());
        fiber.start();
        int total = 5000000;
        final CountDownLatch latch = new CountDownLatch(total);
        final ExecutorService executorService = Executors.newFixedThreadPool(4);
        for (int i = 0; i < total; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    fiber.execute(new Runnable() {
                        @Override
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
            @Override
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
        PipeReader pipePing = new PipeReader() {
            int count = 1;
            private ByteBuffer buffer = ByteBuffer.allocate(4);

            @Override
            protected boolean onData(Pipe.SourceChannel source) {
                try {
                    source.read(buffer);
                    if (buffer.position() == 4) {
                        buffer.flip();
                        assertEquals(count, buffer.getInt());
                        count++;
                        buffer.clear();
                        latch.countDown();
                    }
                    return true;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        final List<NioChannelHandler> pipeReaders = new ArrayList<>();
        pipeReaders.add(pipePing);
        NioFiber fiber = new NioFiberImpl(new NioBatchExecutorImpl(), pipeReaders);
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

    @Test
    public void testWakeup() throws IOException, InterruptedException {
        final Selector selector = Selector.open();
        final CountDownLatch latch = new CountDownLatch(1);
        selector.wakeup();
        new Thread() {
            @Override
            public void run() {
                try {
                    selector.select();
                    latch.countDown();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void merge() {
        ByteBuffer data = ByteBuffer.allocate(2);
        data.put((byte) 5);
        data.flip();
        ByteBuffer merged = NioFiberImpl.addTo(null, data);
        assertEquals(0, merged.position());
        assertEquals(1, merged.limit());
        assertEquals(1, merged.capacity());
        assertEquals(5, merged.get());
        data.clear();
        data.put((byte) 9);
        data.put((byte) 7);
        data.flip();
        merged = NioFiberImpl.addTo(merged, data);
        assertEquals(0, merged.position());
        assertEquals(2, merged.limit());
        assertEquals(3, merged.capacity());
        assertEquals(9, merged.get());
        assertEquals(7, merged.get());
        merged = addByte(merged, 3, 2);
        merged = addByte(merged, 1, 10);

        assertEquals(0, merged.position());
        assertEquals(12, merged.limit());
        assertEquals(13, merged.capacity());
        assertEquals(3, merged.get());
        merged = addByte(merged, 6, 3);
        assertEquals(0, merged.position());
        assertEquals(14, merged.limit());
        assertEquals(3, merged.get());
        for (int i = 0; i < 10; i++)
            assertEquals(1, merged.get());
        for (int i = 0; i < 2; i++)
            assertEquals(6, merged.get());
        assertEquals(1, merged.remaining());

        ByteBuffer prior = merged;
        merged = addByte(merged, 0, 1);
        assertEquals(0, merged.position());
        assertEquals(2, merged.limit());
        assertSame(merged, prior);
    }

    @Test
    public void testRemaining() {
        ByteBuffer byteBuffer = addByte(null, 1, 10);
        assertEquals(10, byteBuffer.remaining());
        byteBuffer = addByte(byteBuffer, 2, 12);
        assertEquals(22, byteBuffer.remaining());
    }

    @Test
    public void mergeOfPartial() {
        ByteBuffer merged = addByte(null, 1, 5);
        merged.get();
        addByte(merged, 1, 2);
    }

    private static ByteBuffer addByte(ByteBuffer merged, int value, int count) {
        ByteBuffer b = ByteBuffer.allocate(count * 2);
        for (int i = 0; i < count; i++) {
            b.put((byte) value);
        }
        b.flip();
        return NioFiberImpl.addTo(merged, b);
    }
}

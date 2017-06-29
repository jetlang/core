package org.jetlang.fibers;

import org.jetlang.core.Callback;
import org.jetlang.core.Disposable;
import org.jetlang.core.EventBuffer;
import org.jetlang.core.QueueSwapper;
import org.jetlang.core.SchedulerImpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class NioFiberImpl implements Runnable, NioFiber {

    private final SchedulerImpl scheduler;
    private final Selector selector;
    private final Map<SelectableChannel, NioState> handlers = new IdentityHashMap<>();
    private final List<Disposable> _disposables = Collections.synchronizedList(new ArrayList<Disposable>());
    private final QueueSwapper queue;
    private final Thread thread;
    private final NioBatchExecutor executor;
    private final WriteFailure writeFailed;

    public interface OnBuffer {

        <T extends SelectableChannel & WritableByteChannel> void onBufferEnd(T channel);

        <T extends SelectableChannel & WritableByteChannel> void onBuffer(T channel, ByteBuffer data);
    }

    private final OnBuffer onBuffer;
    private final NioControls controls = new NioControls() {
        @Override
        public Selector getSelector() {
            return selector;
        }

        @Override
        public boolean isRegistered(SelectableChannel channel) {
            return handlers.containsKey(channel);
        }

        @Override
        public void addHandler(NioChannelHandler handler) {
            synchronousAdd(handler);
        }

        @Override
        public <T extends SelectableChannel & WritableByteChannel> void write(T accept, ByteBuffer buffer) {
            NioState key = handlers.get(accept);
            try {
                if (key == null || key.buffer == null) {
                    writeAll(accept, buffer);
                    if (buffer.remaining() == 0) {
                        return;
                    }
                }
            } catch (IOException e) {
                writeFailed.onFailure(e, accept, buffer);
                return;
            }
            if (key == null) {
                final NioStateWrite handler = new NioStateWrite<>(accept, writeFailed, onBuffer);
                key = synchronousAdd(handler);
                key.buffer = handler;
                handler.state = key;
            } else if (key.buffer == null) {
                final NioStateWrite handler = new NioStateWrite<>(accept, writeFailed, onBuffer);
                handler.state = key;
                key.buffer = handler;
                key.handlers.add(handler);
                key.updateInterest(handler.getInterestSet());
            }
            key.addToBuffer(buffer);
        }

        @Override
        public boolean close(SelectableChannel channel) {
            try {
                channel.close();
            } catch (IOException e) {
            }
            final NioState nioState = handlers.remove(channel);
            if (nioState != null) {
                nioState.onEnd();
                return true;
            }
            return false;
        }
    };

    public static void writeAll(WritableByteChannel channel, ByteBuffer data) throws IOException {
        int write;
        do {
            write = channel.write(data);
        } while (write != 0 && data.remaining() > 0);
    }

    private static class NioState {
        private final SelectableChannel channel;
        private final List<NioChannelHandler> handlers = new ArrayList<>(1);
        private SelectionKey key;

        public BufferedWrite buffer;

        public NioState(SelectableChannel channel) {
            this.channel = channel;
        }

        public void addToBuffer(ByteBuffer newBytes) {
            buffer.buffer(newBytes);
        }

        public void onSelectorEnd() {
            for (NioChannelHandler handler : handlers) {
                handler.onSelectorEnd();
            }
        }

        public boolean onSelect(NioBatchExecutor exec, NioFiberImpl fiber, NioControls controls, SelectionKey key) {
            int size = handlers.size();
            for (int i = 0; i < size; i++) {
                final NioChannelHandler handler = this.handlers.get(i);
                final boolean interested = (key.readyOps() & handler.getInterestSet()) != 0;
                if (interested) {
                    boolean result = exec.runOnSelect(fiber, handler, controls, key);
                    if (!result) {
                        handlers.remove(i--);
                        handler.onEnd();
                        size--;
                        if (!handlers.isEmpty()) {
                            //if no handlers left then the key is going to be cancelled and removed
                            key.interestOps(key.interestOps() & ~handler.getInterestSet());
                        }
                    }
                }
            }
            return !handlers.isEmpty();
        }

        public void onEnd() {
            for (NioChannelHandler handler : handlers) {
                handler.onEnd();
            }
        }

        public void updateInterest(int interestSet) {
            key.interestOps(key.interestOps() | interestSet);
        }
    }

    private static class NioStateWrite<T extends SelectableChannel & WritableByteChannel> extends BufferedWrite<T> {

        NioState state;

        public NioStateWrite(T channel, WriteFailure writeFailed, OnBuffer onBuffer) {
            super(channel, writeFailed, onBuffer);
        }

        @Override
        public void onEnd() {
            state.buffer = null;
            state = null;
        }

    }

    public static class BufferedWrite<T extends SelectableChannel & WritableByteChannel> implements NioChannelHandler {
        private final T channel;
        private final WriteFailure writeFailed;
        private final OnBuffer onBuffer;
        ByteBuffer data;

        public BufferedWrite(T channel, WriteFailure writeFailed, OnBuffer onBuffer) {
            this.channel = channel;
            this.writeFailed = writeFailed;
            this.onBuffer = onBuffer;
        }

        @Override
        public boolean onSelect(NioFiber nioFiber, NioControls controls, SelectionKey key) {
            try {
                writeAll(channel, data);
            } catch (IOException e) {
                writeFailed.onFailure(e, channel, data);
                return false;
            }
            final boolean b = data.remaining() > 0;
            if (!b) {
                onBuffer.onBufferEnd(channel);
            }
            return b;
        }

        @Override
        public SelectableChannel getChannel() {
            return channel;
        }

        @Override
        public int getInterestSet() {
            return SelectionKey.OP_WRITE;
        }

        @Override
        public void onEnd() {

        }

        public ByteBuffer getBuffer() {
            return data;
        }

        @Override
        public void onSelectorEnd() {
            onEnd();
        }

        public int buffer(ByteBuffer buffer) {
            data = addTo(data, buffer);
            assert data.remaining() > 0 : channel + " " + data;
            onBuffer.onBuffer(channel, data);
            return data.remaining();
        }

    }

    public interface WriteFailure {
        <T extends SelectableChannel & WritableByteChannel> void onFailure(IOException e, T channel, ByteBuffer data);
    }

    public static class NoOpWriteFailure implements WriteFailure {

        @Override
        public <T extends SelectableChannel & WritableByteChannel> void onFailure(IOException e, T channel, ByteBuffer data) {

        }
    }

    public static class NoOpBuffer implements OnBuffer {

        @Override
        public <T extends SelectableChannel & WritableByteChannel> void onBufferEnd(T channel) {

        }

        @Override
        public <T extends SelectableChannel & WritableByteChannel> void onBuffer(T channel, ByteBuffer data) {

        }
    }

    public NioFiberImpl() {
        this(new NioBatchExecutorImpl(), Collections.<NioChannelHandler>emptyList());
    }

    public NioFiberImpl(final NioBatchExecutor executor, Collection<NioChannelHandler> handlers) {
        this(executor, handlers, "nioFiber", true, new NoOpWriteFailure(), new NoOpBuffer());
    }

    public NioFiberImpl(final NioBatchExecutor executor, Collection<NioChannelHandler> nioHandlers, String threadName, boolean isDaemonThread, WriteFailure writeFailed, OnBuffer onBuffer) {
        this.executor = executor;
        this.writeFailed = writeFailed;
        this.onBuffer = onBuffer;
        this.scheduler = new SchedulerImpl(this);
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (NioChannelHandler nioHandler : nioHandlers) {
            synchronousAdd(nioHandler);
        }
        queue = new QueueSwapper(selector);
        thread = new Thread(this, threadName);
        thread.setDaemon(isDaemonThread);
    }

    public Thread getThread() {
        return thread;
    }

    public static ByteBuffer addTo(ByteBuffer data, ByteBuffer buffer) {
        final int size = buffer.remaining();
        if (data == null) {
            data = ByteBuffer.allocate(size);
            data.put(buffer);
            data.flip();
        } else {
            data.compact();
            if (data.position() + size > data.capacity()) {
                ByteBuffer b = ByteBuffer.allocate(data.capacity() + size);
                data.flip();
                b.put(data);
                data = b;
            }
            data.put(buffer);
            data.flip();
        }
        return data;
    }

    @Override
    public void execute(Runnable command) {
        queue.put(command);
    }

    @Override
    public void addHandler(final NioChannelHandler handler) {
        execute(new Runnable() {
            @Override
            public void run() {
                NioFiberImpl.this.synchronousAdd(handler);
            }
        });
    }

    @Override
    public void execute(final Callback<NioControls> asyncWrite) {
        execute(new Runnable() {
            @Override
            public void run() {
                asyncWrite.onMessage(controls);
            }
        });
    }

    private NioState synchronousAdd(final NioChannelHandler handler) {
        try {
            final SelectableChannel channel = handler.getChannel();
            channel.configureBlocking(false);
            final NioState nioState = handlers.get(channel);
            if (nioState != null) {
                nioState.handlers.add(handler);
                nioState.updateInterest(handler.getInterestSet());
                return nioState;
            }
            final int interestSet = handler.getInterestSet();
            final NioState value = new NioState(channel);
            value.handlers.add(handler);
            value.key = channel.register(selector, interestSet, value);
            handlers.put(channel, value);
            return value;
        } catch (IOException failed) {
            throw new RuntimeException(failed);
        }
    }

    @Override
    public void add(Disposable disposable) {
        _disposables.add(disposable);
    }

    @Override
    public boolean remove(Disposable disposable) {
        return _disposables.remove(disposable);
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public Disposable schedule(Runnable command, long delay, TimeUnit unit) {
        return scheduler.schedule(command, delay, unit);
    }

    @Override
    public Disposable scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return scheduler.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    @Override
    public Disposable scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduler.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public void dispose() {
        synchronized (_disposables) {
            //copy list to prevent concurrent mod
            for (Disposable r : _disposables.toArray(new Disposable[_disposables.size()])) {
                r.dispose();
            }
        }
        scheduler.dispose();
        try {
            selector.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void start() {
        thread.start();
    }

    @Override
    public void run() {
        EventBuffer buffer = new EventBuffer();
        while (true) {
            try {
                final int select = selector.select();
                if (select > 0) {
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    for (SelectionKey key : selectedKeys) {
                        final NioState attachment = (NioState) key.attachment();
                        if (execEvent(key, attachment)) {
                            handlers.remove(attachment.channel);
                            key.cancel();
                            attachment.onEnd();
                        }
                    }
                    selectedKeys.clear();
                }
                buffer = queue.swap(buffer);
                executor.execute(buffer);
                buffer.clear();
            } catch (ClosedSelectorException closed) {
                queue.setRunning(false);
                break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        for (NioState nioState : handlers.values()) {
            nioState.onSelectorEnd();
        }
        handlers.clear();
    }

    /**
     * @return true if key is finished
     */
    private boolean execEvent(SelectionKey key, NioState attachment) {
        try {
            return !attachment.onSelect(executor, this, controls, key);
        } catch (CancelledKeyException invalid) {
            return true;
        }
    }

}

package org.jetlang.fibers;

import org.jetlang.core.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class NioFiberImpl implements Runnable, NioFiber {

    private final AtomicBoolean started = new AtomicBoolean(false);
    private final SchedulerImpl scheduler;
    private final Selector selector;
    private final List<NioChannelHandler> handlers = new ArrayList<NioChannelHandler>();
    private final String threadName;
    private final boolean isDaemonThread;
    private final List<Disposable> _disposables = Collections.synchronizedList(new ArrayList<Disposable>());
    private final NioQueueSwapper queue;

    public NioFiberImpl() {
        this(new BatchExecutorImpl(), Collections.EMPTY_LIST, "nioFiber", true);
    }

    public NioFiberImpl(final BatchExecutor executor, Collection<NioChannelHandler> nioHandlers, String threadName, boolean isDaemonThread) {
        this.threadName = threadName;
        this.isDaemonThread = isDaemonThread;
        this.scheduler = new SchedulerImpl(this);
        final NioChannelHandler.PipeReader pipe = new NioChannelHandler.PipeReader() {
            EventBuffer buffer = new EventBuffer();
            ByteBuffer bb = ByteBuffer.allocateDirect(2);

            @Override
            protected void onData(Pipe.SourceChannel source) {
                final int read;
                try {
                    read = source.read(bb);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                assert read == 1 : read;
                buffer = queue.swap(buffer);
                executor.execute(buffer);
                buffer.clear();
                bb.clear();
            }
        };
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        synchronousAdd(pipe);
        for (NioChannelHandler nioHandler : nioHandlers) {
            synchronousAdd(nioHandler);
        }
        queue = new NioQueueSwapper(pipe.sink);
    }

    public void execute(Runnable command) {
        queue.put(command);
    }

    public void addHandler(final NioChannelHandler handler) {
        execute(new Runnable() {
            public void run() {
                synchronousAdd(handler);
            }
        });
    }

    private void synchronousAdd(final NioChannelHandler handler) {
        try {
            final SelectableChannel channel = handler.getChannel();
            channel.configureBlocking(false);
            final int interestSet = handler.getInterestSet();
            channel.register(selector, interestSet, handler);
            handlers.add(handler);
        } catch (IOException failed) {
            throw new RuntimeException(failed);
        }
    }

    public void add(Disposable disposable) {
        _disposables.add(disposable);
    }

    public boolean remove(Disposable disposable) {
        return _disposables.remove(disposable);
    }

    public int size() {
        return queue.size();
    }

    public Disposable schedule(Runnable command, long delay, TimeUnit unit) {
        return scheduler.schedule(command, delay, unit);
    }

    public Disposable scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return scheduler.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    public Disposable scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduler.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

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


    public void start() {
        if (started.compareAndSet(false, true)) {
            final Thread thread = new Thread(this, threadName);
            thread.setDaemon(isDaemonThread);
            thread.start();
        } else {
            throw new RuntimeException("Fiber already started");
        }
    }

    public void run() {
        while (true) {
            try {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                for (SelectionKey key : selectedKeys) {
                    ((NioChannelHandler) key.attachment()).onSelect(key);
                }
                selectedKeys.clear();
            } catch (ClosedSelectorException closed) {
                queue.setRunning(false);
                break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        for (NioChannelHandler handler : handlers) {
            handler.onEnd();
        }
    }

}

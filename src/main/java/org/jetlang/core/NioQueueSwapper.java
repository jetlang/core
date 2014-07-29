package org.jetlang.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

public class NioQueueSwapper {
    private final Pipe.SinkChannel sink;
    private final ByteBuffer pingByte = ByteBuffer.allocateDirect(1);
    private EventBuffer _queue = new EventBuffer();
    private boolean running = true;

    public NioQueueSwapper(Pipe.SinkChannel sink) {
        this.sink = sink;
        this.pingByte.limit(1);
    }


    public synchronized void put(Runnable r) {
        if (!running) {
            return;
        }
        _queue.add(r);
        if (_queue.size() == 1) {
            pingByte.position(0);
            try {
                sink.write(pingByte);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized EventBuffer swap(EventBuffer buffer) {
        EventBuffer toReturn = _queue;
        _queue = buffer;
        return toReturn;
    }

    public synchronized int size() {
        return _queue.size();
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
    }
}

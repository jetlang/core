package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.Filter;
import org.jetlang.core.MessageBuffer;
import org.jetlang.core.MessageReader;
import org.jetlang.fibers.Fiber;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Batches events for the consuming thread.
 */
public class RecyclingBatchSubscriber<T> extends BaseSubscription<T> {
    private final Lock _lock = new ReentrantLock();
    private final Fiber _queue;
    private final Callback<MessageReader<T>> _receive;
    private final int _interval;
    private final TimeUnit _timeUnit;
    private MessageBuffer<T> _pending = new MessageBuffer<>();
    private MessageBuffer<T> _active = new MessageBuffer<>();
    private final Runnable _flushRunnable;

    public RecyclingBatchSubscriber(Fiber queue, Callback<MessageReader<T>> receive,
                                    Filter<T> filter,
                                    int interval, TimeUnit timeUnit) {
        super(queue, filter);
        _queue = queue;
        _receive = receive;
        _interval = interval;
        _timeUnit = timeUnit;
        _flushRunnable = new Runnable() {
            public void run() {
                flush();
            }

            @Override
            public String toString() {
                return "Flushing " + RecyclingBatchSubscriber.this + " via " +  _receive.toString();
            }
        };
    }

    public RecyclingBatchSubscriber(Fiber queue, Callback<MessageReader<T>> receive,
                                    int interval, TimeUnit timeUnit) {
        this(queue, receive, null, interval, timeUnit);
    }

    /**
     * Receives message and batches as needed.
     */
    @Override
    protected void onMessageOnProducerThread(T msg) {
        _lock.lock();
        try {
            if (_pending.isEmpty()) {
                _queue.schedule(_flushRunnable, _interval, _timeUnit);
            }
            _pending.add(msg);
        } finally {
            _lock.unlock();
        }
    }

    private void flush() {
        _lock.lock();
        try {
            MessageBuffer<T> nowPending = _active;
            _active = _pending;
            _pending = nowPending;
        } finally {
            _lock.unlock();
        }
        try {
            _receive.onMessage(_active);
        } finally {
            _active.clear();
        }
    }
}
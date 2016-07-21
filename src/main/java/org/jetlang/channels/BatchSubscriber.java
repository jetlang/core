package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.Filter;
import org.jetlang.fibers.Fiber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Batches events for the consuming thread.
 */
public class BatchSubscriber<T> extends BaseSubscription<T> {
    private final Object _lock = new Object();
    private final Fiber _queue;
    private final Callback<List<T>> _receive;
    private final int _interval;
    private final TimeUnit _timeUnit;
    private List<T> _pending;
    private final Runnable _flushRunnable;

    public BatchSubscriber(Fiber queue, Callback<List<T>> receive,
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
                return "Flushing " + BatchSubscriber.this + " via " +  _receive.toString();
            }
        };
    }

    public BatchSubscriber(Fiber queue, Callback<List<T>> receive,
                           int interval, TimeUnit timeUnit) {
        this(queue, receive, null, interval, timeUnit);
    }

    /**
     * Receives message and batches as needed.
     */
    @Override
    protected void onMessageOnProducerThread(T msg) {
        synchronized (_lock) {
            if (_pending == null) {
                _pending = new ArrayList<>();
                _queue.schedule(_flushRunnable, _interval, _timeUnit);
            }
            _pending.add(msg);
        }
    }

    private void flush() {
        List<T> toFlush = null;
        synchronized (_lock) {
            if (_pending != null) {
                toFlush = _pending;
                _pending = null;
            }
        }
        if (toFlush != null) {
            _receive.onMessage(toFlush);
        }
    }
}

package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.fibers.Fiber;

import java.util.concurrent.TimeUnit;

/**
 * Subscribes to last event received on the channel. If consuming thread cannot
 * process events as fast as they arrive, then older events will be dropped in favor
 * of most recent.  Flush interval determines rate at which events are processes. If interval is less than 1, then
 * events will be delivered as fast as consuming thread can handle them.
 */
public class LastSubscriber<T> extends BaseSubscription<T> {
    private final Object _lock = new Object();

    private final Fiber _context;
    private final Callback<T> _target;
    private final int _flushIntervalInMs;
    private final TimeUnit _timeUnit;

    private boolean _flushPending;
    private T _pending;
    private final Runnable _flushRunnable;

    public LastSubscriber(Callback<T> target, Fiber context, int flushIntervalInMs, TimeUnit timeUnit) {
        super(context);
        _context = context;
        _target = target;
        _flushIntervalInMs = flushIntervalInMs;
        _timeUnit = timeUnit;
        _flushRunnable = new Runnable() {
            public void run() {
                flush();
            }
        };
    }

    @Override
    protected void onMessageOnProducerThread(T msg) {
        synchronized (_lock) {
            if (!_flushPending) {
                _context.schedule(_flushRunnable, _flushIntervalInMs, _timeUnit);
                _flushPending = true;
            }
            _pending = msg;
        }
    }

    private void flush() {
        T toReturn = clearPending();
        _target.onMessage(toReturn);
    }

    private T clearPending() {
        synchronized (_lock) {
            _flushPending = false;
            return _pending;
        }
    }
}
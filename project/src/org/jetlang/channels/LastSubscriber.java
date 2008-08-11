package org.jetlang.channels;    /// <summary>
/// Subscribes to last event received on the channel.
/// </summary>
/// <typeparam name="T"></typeparam>

import org.jetlang.core.Callback;
import org.jetlang.fibers.Fiber;

import java.util.concurrent.TimeUnit;

public class LastSubscriber<T> extends BaseSubscription<T> {
    private final Object _lock = new Object();

    private final Fiber _context;
    private final Callback<T> _target;
    private final int _flushIntervalInMs;

    private boolean _flushPending;
    private T _pending;
    private final Runnable _flushRunnable;

    /// <summary>
    /// New instance.
    /// </summary>
    /// <param name="target"></param>
    /// <param name="context"></param>
    /// <param name="flushIntervalInMs"></param>
    public LastSubscriber(Callback<T> target, Fiber context, int flushIntervalInMs) {
        super(context);
        _context = context;
        _target = target;
        _flushIntervalInMs = flushIntervalInMs;
        _flushRunnable = new Runnable() {
            public void run() {
                Flush();
            }
        };
    }

    /// <summary>
    /// Receives message from producer thread.
    /// </summary>
    /// <param name="msg"></param>
    @Override
    protected void onMessageOnProducerThread(T msg) {
        synchronized (_lock) {
            if (!_flushPending) {
                _context.schedule(_flushRunnable, _flushIntervalInMs, TimeUnit.MILLISECONDS);
                _flushPending = true;
            }
            _pending = msg;
        }
    }

    /// <summary>
    /// Flushes on IProcessTimer thread.
    /// </summary>
    private void Flush() {
        T toReturn = ClearPending();
        _target.onMessage(toReturn);
    }

    private T ClearPending() {
        synchronized (_lock) {
            _flushPending = false;
            return _pending;
        }
    }
}
package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.Filter;
import org.jetlang.fibers.Fiber;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Channel subscription that drops duplicates based upon a key.
 */
public class KeyedBatchSubscriber<K, T> extends BaseSubscription<T> {
    private final Object _batchLock = new Object();

    private final Fiber _context;
    private final Callback<Map<K, T>> _target;
    private final int _flushIntervalInMs;
    private final TimeUnit _timeUnit;
    private final Converter<T, K> _keyResolver;

    private Map<K, T> _pending = null;
    private final Runnable _flushRunner;

    public KeyedBatchSubscriber(Fiber context,
                                Callback<Map<K, T>> target,
                                Filter<T> filter,
                                int flushIntervalInMs, TimeUnit timeUnit,
                                Converter<T, K> keyResolver) {
        super(context, filter);
        _keyResolver = keyResolver;
        _context = context;
        _target = target;
        _flushIntervalInMs = flushIntervalInMs;
        _timeUnit = timeUnit;
        _flushRunner = new Runnable() {
            public void run() {
                flush();
            }

            @Override
            public String toString() {
                return "Flushing " + KeyedBatchSubscriber.this + " via " +  _target.toString();
            }
        };
    }

    public KeyedBatchSubscriber(Fiber context, Callback<Map<K, T>> target,
                                int flushIntervalInMs, TimeUnit timeUnit,
                                Converter<T, K> keyResolver) {
        this(context, target, null, flushIntervalInMs, timeUnit, keyResolver);
    }

    /**
     * Message received and batched on producer thread.
     */
    @Override
    protected void onMessageOnProducerThread(T msg) {
        synchronized (_batchLock) {
            K key = _keyResolver.convert(msg);
            if (_pending == null) {
                _pending = new HashMap<K, T>();
                _context.schedule(_flushRunner, _flushIntervalInMs, _timeUnit);
            }
            _pending.put(key, msg);
        }
    }


    /**
     * Flushes events on fiber thread
     */
    private void flush() {
        Map<K, T> toReturn = clearPending();
        if (toReturn != null) {
            _target.onMessage(toReturn);
        }
    }

    private Map<K, T> clearPending() {
        synchronized (_batchLock) {
            if (_pending == null || _pending.isEmpty()) {
                _pending = null;
                return null;
            }
            Map<K, T> toReturn = _pending;
            _pending = null;
            return toReturn;
        }
    }
}

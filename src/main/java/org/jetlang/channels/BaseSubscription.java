package org.jetlang.channels;

import org.jetlang.core.DisposingExecutor;
import org.jetlang.core.Filter;

/**
 * Base implementation for all producer thread subscriptions.
 */
public abstract class BaseSubscription<T> implements Subscribable<T> {

    private final Filter<T> _filter;
    private final DisposingExecutor fiber;

    public BaseSubscription(DisposingExecutor fiber) {
        this.fiber = fiber;
        _filter = null;
    }

    protected BaseSubscription(DisposingExecutor fiber, Filter<T> _filter) {
        this._filter = _filter;
        this.fiber = fiber;
    }

    public DisposingExecutor getQueue() {
        return fiber;
    }

    /**
     * Receives the event, filters, and passes to handler.
     */
    public void onMessage(T msg) {
        if (_filter == null || _filter.passes(msg)) {
            onMessageOnProducerThread(msg);
        }
    }

    protected abstract void onMessageOnProducerThread(T msg);

}

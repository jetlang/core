package org.jetlang.channels;

import org.jetlang.core.DisposingExecutor;
import org.jetlang.core.Filter;

/**
 * Base implementation for all producer thread subscriptions.
 */
public abstract class BaseSubscription<T> implements Subscribable<T> {

    private Filter<T> _filter;
    private DisposingExecutor fiber;

    public BaseSubscription(DisposingExecutor fiber) {
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

    /**
     * Set a filter to be invoked from producer thread. Events not passing
     * the filter will be discarded and not delivered to consuming Fiber.
     *
     * @param filter
     */
    public void setFilterOnProducerThread(Filter<T> filter) {
        _filter = filter;
    }
}

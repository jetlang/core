package org.jetlang.channels;

import org.jetlang.core.DisposingExecutor;
import org.jetlang.core.Filter;

/// <summary>
/// Subscription for events on a channel.
/// </summary>

/// <typeparam name="T"></typeparam>
public abstract class BaseSubscription<T> implements Subscribable<T> {

    private Filter<T> _filter;
    private DisposingExecutor fiber;

    public BaseSubscription(DisposingExecutor fiber) {
        this.fiber = fiber;
    }

    public DisposingExecutor getQueue() {
        return fiber;
    }

    /// <summary>
    /// Receives the event and queues the execution on the target execute.
    /// </summary>
    /// <param name="msg"></param>
    public void onMessage(T msg) {
        if (_filter == null || _filter.passes(msg)) {
            onMessageOnProducerThread(msg);
        }
    }

    protected abstract void onMessageOnProducerThread(T msg);

    public void setFilterOnProducerThread(Filter<T> filter) {
        _filter = filter;
    }
}

package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.Filter;
import org.jetlang.core.RunnableQueue;

/// <summary>
/// Subscription for events on a channel.
/// </summary>

/// <typeparam name="T"></typeparam>
public class ChannelSubscription<T> implements Callback<T> {
    private final Callback<T> _receiveMethod;
    private final RunnableQueue _targetQueue;
    private Filter<T> _filter;

    /// <summary>
    /// Construct the subscription
    /// </summary>
    /// <param name="execute"></param>
    /// <param name="receiveMethod"></param>
    public ChannelSubscription(RunnableQueue queue, Callback<T> receiveMethod) {
        _receiveMethod = receiveMethod;
        _targetQueue = queue;
    }

    /// <summary>
    /// Receives the event and queues the execution on the target execute.
    /// </summary>
    /// <param name="msg"></param>
    public void onMessage(final T msg) {
        if (_filter == null || _filter.passes(msg)) {
            Runnable asyncExec = new Runnable() {
                public void run() {
                    _receiveMethod.onMessage(msg);
                }
            };
            _targetQueue.execute(asyncExec);
        }
    }

    public void setFilterOnProducerThread(Filter<T> filter) {
        _filter = filter;
    }
}

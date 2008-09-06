package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.DisposingExecutor;
import org.jetlang.core.Filter;

/**
 * Subscription for events on a channel.
 */
public class ChannelSubscription<T> extends BaseSubscription<T> {

    private final Callback<T> _receiveMethod;

    public ChannelSubscription(DisposingExecutor queue, Callback<T> receiveMethod) {
        this(queue, receiveMethod, null);
    }

    public ChannelSubscription(DisposingExecutor fiber, Callback<T> receiveMethod,
                               Filter<T> filter) {
        super(fiber, filter);
        this._receiveMethod = receiveMethod;
    }

    /**
     * Receives the event and queues the execution on the target execute.
     */
    @Override
    protected void onMessageOnProducerThread(final T msg) {
        Runnable asyncExec = new Runnable() {
            public void run() {
                _receiveMethod.onMessage(msg);
            }
        };
        getQueue().execute(asyncExec);
    }
}

package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.DisposingExecutor;

/// <summary>
/// Subscription for events on a channel.
/// </summary>

/// <typeparam name="T"></typeparam>
class ChannelSubscription<T> extends BaseSubscription<T> {
    private final Callback<T> _receiveMethod;

    /// <summary>
    /// Construct the subscription
    /// </summary>
    /// <param name="execute"></param>
    /// <param name="receiveMethod"></param>
    public ChannelSubscription(DisposingExecutor queue, Callback<T> receiveMethod) {
        super(queue);
        _receiveMethod = receiveMethod;
    }

    /// <summary>
    /// Receives the event and queues the execution on the target execute.
    /// </summary>
    /// <param name="msg"></param>
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

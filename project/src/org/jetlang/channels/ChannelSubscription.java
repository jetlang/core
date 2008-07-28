package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.RunnableQueue;

/// <summary>
/// Subscription for events on a channel.
/// </summary>

/// <typeparam name="T"></typeparam>
public class ChannelSubscription<T> extends BaseSubscription<T> {
    private final Callback<T> _receiveMethod;

    /// <summary>
    /// Construct the subscription
    /// </summary>
    /// <param name="execute"></param>
    /// <param name="receiveMethod"></param>
    public ChannelSubscription(RunnableQueue queue, Callback<T> receiveMethod) {
        super(queue);
        _receiveMethod = receiveMethod;
    }

    /// <summary>
    /// Receives the event and queues the execution on the target execute.
    /// </summary>
    /// <param name="msg"></param>
    protected void onMessageOnProducerThread(final T msg) {
        Runnable asyncExec = new Runnable() {
            public void run() {
                _receiveMethod.onMessage(msg);
            }
        };
        getQueue().execute(asyncExec);
    }
}

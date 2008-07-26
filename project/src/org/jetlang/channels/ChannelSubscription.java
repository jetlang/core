package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.RunnableQueue;

/// <summary>
/// Subscription for events on a channel.
/// </summary>

/// <typeparam name="T"></typeparam>
public class ChannelSubscription<T> {
    private final Callback<T> _receiveMethod;
    private final RunnableQueue _targetQueue;

    /// <summary>
    /// Construct the subscription
    /// </summary>
    /// <param name="queue"></param>
    /// <param name="receiveMethod"></param>
    public ChannelSubscription(RunnableQueue queue, Callback<T> receiveMethod) {
        _receiveMethod = receiveMethod;
        _targetQueue = queue;
    }

    /// <summary>
    /// Receives the event and queues the execution on the target queue.
    /// </summary>
    /// <param name="msg"></param>
    protected void OnMessageOnProducerThread(final T msg) {
        Runnable asyncExec = new Runnable() {
            public void run() {
                _receiveMethod.onMessage(msg);
            }
        };
        _targetQueue.queue(asyncExec);
    }
}

package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.ICommandQueue;
import org.jetlang.core.Unsubscriber;

/// <summary>
/// Channel subscription methods.
/// </summary>

/// <typeparam name="T"></typeparam>
public interface ChannelSubscriber<T> {
    ///<summary>
    /// subscribe to messages on this channel. The provided action will be invoked via a command on the provided queue.
    ///</summary>
    ///<param name="queue">the target context to receive the message</param>
    ///<param name="receive"></param>
    ///<returns>Unsubscriber object</returns>
    Unsubscriber subscribe(ICommandQueue queue, Callback<T> receive);

    /// <summary>
    /// Removes all subscribers.
    /// </summary>
    void clearSubscribers();

    /// <summary>
    /// Subscribes to events on the channel in batch form. The events will be batched if the consumer is unable to process the events
    /// faster than the arrival rate.
    /// </summary>
    /// <param name="queue">The target context to execute the action</param>
    /// <param name="receive"></param>
    /// <param name="intervalInMs">Time in Ms to batch events. If 0 events will be delivered as fast as consumer can process</param>
    /// <returns></returns>
    //IUnsubscriber SubscribeToBatch(ICommandTimer queue, Callback<List<T>> receive, int intervalInMs);

    ///<summary>
    /// Batches events based upon keyed values allowing for duplicates to be dropped.
    ///</summary>
    ///<param name="queue"></param>
    ///<param name="keyResolver"></param>
    ///<param name="receive"></param>
    ///<param name="intervalInMs"></param>
    ///<typeparam name="K"></typeparam>
    ///<returns></returns>
    //<K> IUnsubscriber SubscribeToKeyedBatch(ICommandTimer queue, Converter<T, K> keyResolver, Callback<Dictionary<K, T>> receive, int intervalInMs);

    /// <summary>
    /// Subscription that delivers the latest message to the consuming thread.  If a newer message arrives before the consuming thread
    /// has a chance to process the message, the pending message is replaced by the newer message. The old message is discarded.
    /// </summary>
    /// <param name="queue"></param>
    /// <param name="receive"></param>
    /// <param name="intervalInMs"></param>
    /// <returns></returns>
    //IUnsubscriber SubscribeToLast(ICommandTimer queue, Callback<T> receive, int intervalInMs);

    /// <summary>
    /// Subscribes to messages on producer threads. Action will be invoked on producer thread. Action must
    /// be thread safe.
    /// </summary>
    /// <param name="subscriber"></param>
    /// <returns></returns>
    //IUnsubscriber SubscribeOnProducerThreads(Callback<T> subscriber);

    /// <summary>
    /// Subscribes to events on producer threads. Subscriber could be called from multiple threads.
    /// </summary>
    /// <param name="subscriber"></param>
    /// <returns></returns>
    //IUnsubscriber SubscribeOnProducerThreads(IProducerThreadSubscriber<T> subscriber);
}
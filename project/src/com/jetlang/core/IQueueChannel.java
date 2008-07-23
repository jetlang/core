package com.jetlang.core;

/**
 * Created by IntelliJ IDEA.
* User: mrettig
* Date: Jul 22, 2008
* Time: 2:58:03 PM
* To change this template use File | Settings | File Templates.
*/ /// <summary>
/// Creates a queue that will deliver a message to a single consumer. Load balancing can be achieved by creating
/// multiple subscribers to the queue.
/// </summary>
/// <typeparam name="T"></typeparam>
public interface IQueueChannel<T>
{
    /// <summary>
    /// subscribe to the queue.
    /// </summary>
    /// <param name="queue"></param>
    /// <param name="onMessage"></param>
    /// <returns></returns>
    IUnsubscriber Subscribe(ICommandQueue queue, Callback<T> onMessage);

    /// <summary>
    /// Pushes a message into the queue. Message will be processed by first available consumer.
    /// </summary>
    /// <param name="message"></param>
    void Publish(T message);
}

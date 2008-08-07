package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.Disposable;
import org.jetlang.core.RunnableQueue;

/// <summary>
/// Channel subscription methods.
/// </summary>

/// <typeparam name="T"></typeparam>
public interface ChannelSubscriber<T> {
    ///<summary>
    /// subscribe to messages on this channel. The provided action will be invoked via a command on the provided execute.
    ///</summary>
    ///<param name="execute">the target context to receive the message</param>
    ///<param name="receive"></param>
    ///<returns>Disposable object</returns>
    Disposable subscribe(RunnableQueue queue, Callback<T> receive);

    /// <summary>
    /// Removes all subscribers.
    /// </summary>
    void clearSubscribers();

}
package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.RunnableQueue;
import org.jetlang.core.Stopable;

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
    ///<returns>Stopable object</returns>
    Stopable subscribe(RunnableQueue queue, Callback<T> receive);

    /// <summary>
    /// Removes all subscribers.
    /// </summary>
    void clearSubscribers();

}
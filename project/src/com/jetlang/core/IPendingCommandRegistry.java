package com.jetlang.core;

/**
 * Created by IntelliJ IDEA.
* User: mrettig
* Date: Jul 22, 2008
* Time: 2:58:02 PM
* To change this template use File | Settings | File Templates.
*/ /// <summary>
/// Stores and removes pending commands.
/// </summary>
public interface IPendingCommandRegistry
{
    /// <summary>
    /// Remove timer
    /// </summary>
    /// <param name="timer"></param>
    void Remove(ITimerControl timer);

    /// <summary>
    /// Queue event to target queue.
    /// </summary>
    /// <param name="command"></param>
    void EnqueueTask(Runnable command);
}

package org.jetlang.core;

/**
 * User: mrettig
 * Date: Jul 22, 2008
 * Time: 2:58:02 PM
 */ /// <summary>
/// Stores and removes pending commands.
/// </summary>
public interface IPendingCommandRegistry {
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

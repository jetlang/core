package org.jetlang.core;

/// <summary>
/// Queue for command objects.

/// </summary>
public interface RunnableQueue {
    /// <summary>
    /// Enqueue a single command.
    /// </summary>
    /// <param name="command"></param>
    void queue(Runnable command);

    void onStop(Runnable runOnStop);
}

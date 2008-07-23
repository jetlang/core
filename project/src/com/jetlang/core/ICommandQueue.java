package com.jetlang.core;

/// <summary>
/// Queue for command objects.
/// </summary>
public interface ICommandQueue
{
    /// <summary>
    /// Enqueue a single command.
    /// </summary>
    /// <param name="command"></param>
    void queue(Runnable command);
}

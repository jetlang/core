package org.jetlang.core;

public interface ProcessFiber extends ICommandQueue, RunnableScheduler, Stopable {
    /// <summary>
    /// start consuming events.
    /// </summary>
    void start();
}

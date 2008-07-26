package org.jetlang.core;

public interface ProcessFiber extends RunnableQueue, RunnableScheduler, Stopable {
    /// <summary>
    /// start consuming events.
    /// </summary>
    void start();
}

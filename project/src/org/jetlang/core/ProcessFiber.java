package org.jetlang.core;

public interface ProcessFiber extends ICommandQueue, ICommandTimer, Stopable {
    /// <summary>
    /// start consuming events.
    /// </summary>
    void start();
}

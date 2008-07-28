package org.jetlang.fibers;

import org.jetlang.core.RunnableQueue;
import org.jetlang.core.RunnableScheduler;
import org.jetlang.core.Stopable;

public interface ProcessFiber extends RunnableQueue, RunnableScheduler, Stopable {
    /// <summary>
    /// start consuming events.
    /// </summary>
    void start();
}

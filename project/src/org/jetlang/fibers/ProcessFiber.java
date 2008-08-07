package org.jetlang.fibers;

import org.jetlang.core.Disposable;
import org.jetlang.core.RunnableQueue;
import org.jetlang.core.RunnableScheduler;

public interface ProcessFiber extends RunnableQueue, RunnableScheduler, Disposable {
    /// <summary>
    /// start consuming events.
    /// </summary>
    void start();
}

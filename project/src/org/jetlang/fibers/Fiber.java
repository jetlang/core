package org.jetlang.fibers;

import org.jetlang.core.Disposable;
import org.jetlang.core.RunnableQueue;
import org.jetlang.core.RunnableScheduler;


public interface Fiber extends RunnableQueue, RunnableScheduler, Disposable {
    /**
     * Start consuming events
     */
    void start();
}

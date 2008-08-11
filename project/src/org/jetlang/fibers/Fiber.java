package org.jetlang.fibers;

import org.jetlang.core.DisposingExecutor;
import org.jetlang.core.RunnableScheduler;


public interface Fiber extends DisposingExecutor, RunnableScheduler {
    /**
     * Start consuming events
     */
    void start();
}

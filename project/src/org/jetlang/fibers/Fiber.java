package org.jetlang.fibers;

import org.jetlang.core.DisposingExecutor;
import org.jetlang.core.Scheduler;


public interface Fiber extends DisposingExecutor, Scheduler {
    /**
     * Start consuming events
     */
    void start();
}

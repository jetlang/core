package org.jetlang.fibers;

import org.jetlang.core.DisposingExecutor;
import org.jetlang.core.Scheduler;


/**
 * Fibers provide event queueing, scheduling, and full pub/sub capabilities when combined with a
 * {@see org.jetlang.channels.Channel}. Fibers can be backed by a dedicated thread ({@see ThreadFiber}) or use
 * a thread pool ({@see PoolFiberFactory}).
 * <p/>
 * Events executed by Fiber will be process sequentially.
 *
 * @author mrettig
 */
public interface Fiber extends DisposingExecutor, Scheduler {
    /**
     * Start consuming events
     */
    void start();
}

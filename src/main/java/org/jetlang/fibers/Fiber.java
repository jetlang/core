package org.jetlang.fibers;

import org.jetlang.core.DisposingExecutor;
import org.jetlang.core.Scheduler;


/**
 * Fibers provide event queueing, scheduling, and full pub/sub capabilities when combined with a
 * {@link org.jetlang.channels.Channel}. Fibers can be backed by a dedicated thread ({@link ThreadFiber}) or use
 * a thread pool ({@link PoolFiberFactory}).
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

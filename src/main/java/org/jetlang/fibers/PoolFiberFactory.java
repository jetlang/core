package org.jetlang.fibers;

import org.jetlang.core.*;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Factory that creates {@link Fiber} instances that share threads.
 */
public class PoolFiberFactory implements Disposable {

    private final ScheduledExecutorService _scheduler;
    private final Executor executor;

    /**
     * Construct a new instance.
     *
     * @param executor Executor to use for flushing pending commands for each created Fiber
     */
    public PoolFiberFactory(Executor executor, ScheduledExecutorService sched) {
        this.executor = executor;
        this._scheduler = sched;
    }

    public PoolFiberFactory(Executor exec) {
        this(exec, SchedulerImpl.createSchedulerThatIgnoresEventsAfterStop());
    }

    /**
     * Create a new Fiber from this pool. Equivalent to calling {@link #create(BatchExecutor)}
     * with a {@link SynchronousExecutor}
     *
     * @return Fiber instance
     */
    public Fiber create() {
        return create(new BatchExecutorImpl());
    }

    /**
     * Create a new Fiber from this pool that uses the supplied {@link Executor} to execute commands
     *
     * @param batchExecutor Executor to use for command executor. Required.
     * @return Fiber instance
     */
    public Fiber create(BatchExecutor batchExecutor) {
        return new PoolFiber(this.executor, batchExecutor, _scheduler);
    }

    public void dispose() {
        _scheduler.shutdown();
    }
}

package org.jetlang.fibers;

import org.jetlang.core.Disposable;
import org.jetlang.core.SynchronousExecutor;

import java.util.Timer;
import java.util.concurrent.Executor;

/**
 * Factory that creates {@link Fiber} instances that share threads.
 */
public class PoolFiberFactory implements Disposable {

    private final Timer _scheduler = new Timer(true);
    private final Executor executor;

    /**
     * Construct a new instance.
     *
     * @param executor Executor to use for flushing pending commands for each created Fiber
     */
    public PoolFiberFactory(Executor executor) {
        this.executor = executor;
    }

    /**
     * Create a new Fiber from this pool. Equivalent to calling {@link #create(Executor)}
     * with a {@link SynchronousExecutor}
     *
     * @return Fiber instance
     */
    public Fiber create() {
        return create(new SynchronousExecutor());
    }

    /**
     * Create a new Fiber from this pool that uses the supplied {@link Executor} to execute commands
     *
     * @param executor Executor to use for command executor. Required.
     * @return Fiber instance
     */
    public Fiber create(Executor executor) {
        return new PoolFiber(this.executor, executor, _scheduler);
    }

    public void dispose() {
        _scheduler.cancel();
    }
}

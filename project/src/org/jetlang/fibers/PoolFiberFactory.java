package org.jetlang.fibers;

import org.jetlang.core.Disposable;
import org.jetlang.core.SynchronousExecutor;

import java.util.Timer;
import java.util.concurrent.Executor;

/**
 * User: mrettig
 * Date: Jul 27, 2008
 * Time: 9:34:31 PM
 */
public class PoolFiberFactory implements Disposable {

    private final Timer _scheduler = new Timer(true);
    private final Executor executor;

    public PoolFiberFactory(Executor executor) {
        this.executor = executor;
    }

    public PoolFiber create(Executor invoker) {
        return new PoolFiber(executor, invoker, _scheduler);
    }

    public void dispose() {
        _scheduler.cancel();
    }

    public Fiber create() {
        return create(new SynchronousExecutor());
    }
}

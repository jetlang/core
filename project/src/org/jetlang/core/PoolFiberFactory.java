package org.jetlang.core;

import java.util.Timer;
import java.util.concurrent.Executor;

/**
 * User: mrettig
 * Date: Jul 27, 2008
 * Time: 9:34:31 PM
 */
public class PoolFiberFactory implements Stopable {

    private final Timer _scheduler = new Timer(true);
    private Executor executor;

    public PoolFiberFactory(Executor executor) {
        this.executor = executor;
    }

    public PoolFiber create(RunnableInvoker invoker) {
        return new PoolFiber(executor, invoker, _scheduler);
    }

    public void stop() {
        _scheduler.cancel();
    }
}

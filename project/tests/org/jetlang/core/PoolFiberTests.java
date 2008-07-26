package org.jetlang.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: mrettig
 * Date: Jul 23, 2008
 * Time: 8:04:04 PM
 */
public class PoolFiberTests extends FiberBaseTest {

    private ExecutorService _executor;

    public ProcessFiber CreateBus() {
        return new PoolFiber(_executor, new CommandExecutor());
    }

    public void DoSetup() {
        _executor = Executors.newCachedThreadPool();
    }

    public void DoTearDown() {
        if (_executor != null)
            _executor.shutdown();
    }
}

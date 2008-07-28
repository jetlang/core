package org.jetlang.core;

import org.jetlang.fibers.PoolFiberFactory;
import org.jetlang.fibers.ProcessFiber;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: mrettig
 * Date: Jul 23, 2008
 * Time: 8:04:04 PM
 */
public class PoolFiberTests extends FiberBaseTest {

    private ExecutorService _executor;
    private PoolFiberFactory _fiberFactory;

    public ProcessFiber CreateBus() {
        return _fiberFactory.create(new RunnableInvokerImpl());
    }

    public void DoSetup() {
        _executor = Executors.newCachedThreadPool();
        _fiberFactory = new PoolFiberFactory(_executor);
    }

    public void DoTearDown() {
        if (_executor != null)
            _executor.shutdown();
        if (_fiberFactory != null) {
            _fiberFactory.stop();
        }
    }

    @Test
    public void nothing() {

    }
}

package org.jetlang.tests;

import org.jetlang.core.Disposable;
import org.jetlang.core.RunnableInvokerImpl;
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
            _fiberFactory.dispose();
        }
    }

    @Test
    public void ScheduleIntervalWithCancel() throws InterruptedException {
        _bus.start();
        Runnable onReset = new Runnable() {
            public void run() {
            }
        };
        Disposable stopper = _bus.scheduleOnInterval(onReset, 15, 15);
        assertEquals(1, _bus.stoppableSize());
        stopper.dispose();
        assertEquals(0, _bus.stoppableSize());

    }

}
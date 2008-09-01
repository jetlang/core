package org.jetlang.tests;

import org.jetlang.core.Disposable;
import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.PoolFiberFactory;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * User: mrettig
 * Date: Jul 23, 2008
 * Time: 8:04:04 PM
 */
public class PoolFiberTest extends FiberBaseTest {

    private ExecutorService _executor;
    private PoolFiberFactory _fiberFactory;

    @Override
    public Fiber createFiber() {
        return _fiberFactory.create();
    }

    @Override
    public void doSetup() {
        _executor = Executors.newCachedThreadPool();
        _fiberFactory = new PoolFiberFactory(_executor);
    }

    @Override
    public void doTearDown() {
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
        Disposable stopper = _bus.scheduleWithFixedDelay(onReset, 15, 15, TimeUnit.MILLISECONDS);
        assertEquals(1, _bus.size());
        stopper.dispose();
        assertEquals(0, _bus.size());

    }

}

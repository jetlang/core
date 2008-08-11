package org.jetlang.tests;

import org.jetlang.core.Disposable;
import org.jetlang.core.RunnableExecutorImpl;
import org.jetlang.fibers.Fiber;
import org.jetlang.fibers.ThreadFiber;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * User: mrettig
 * Date: Jul 23, 2008
 * Time: 8:40:58 PM
 */
public class ThreadFiberTests extends FiberBaseTest {

    @Override
    public Fiber CreateBus() {
        return new ThreadFiber(new RunnableExecutorImpl(), System.currentTimeMillis() + "", true);
    }

    @Override
    public void DoSetup() {
    }

    @Override
    public void DoTearDown() {
    }

    @Test
    public void ScheduleIntervalWithCancel() throws InterruptedException {
        _bus.start();
        Runnable onReset = new Runnable() {
            public void run() {
            }
        };
        Disposable stopper = _bus.scheduleWithFixedDelay(onReset, 15, 15, TimeUnit.MILLISECONDS);
        assertEquals(0, _bus.size());
        stopper.dispose();
        assertEquals(0, _bus.size());
    }

}

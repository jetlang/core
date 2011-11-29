package org.jetlang.fibers;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * User: mrettig
 * Date: 11/29/11
 * Time: 4:50 PM
 */
public class FiberStubTest {

    Runnable emptyRunnable = new Runnable() {
        public void run() {

        }
    };

    @Test
    public void nonRecurring() {
        FiberStub stub = new FiberStub();
        stub.schedule(emptyRunnable, 1, TimeUnit.SECONDS);
        assertEquals(1, stub.Scheduled.size());
        assertFalse(stub.Scheduled.get(0).isRecurring());
        stub.executeAllScheduled();
        assertEquals(0, stub.Scheduled.size());
    }

    @Test
    public void recurring() {
        FiberStub stub = new FiberStub();
        stub.scheduleAtFixedRate(emptyRunnable, 1, 1, TimeUnit.SECONDS);
        assertEquals(1, stub.Scheduled.size());
        assertTrue(stub.Scheduled.get(0).isRecurring());
        stub.executeAllScheduled();
        assertEquals(1, stub.Scheduled.size());
    }

    @Test
    public void recurring2() {
        FiberStub stub = new FiberStub();
        stub.scheduleWithFixedDelay(emptyRunnable, 1, 1, TimeUnit.SECONDS);
        assertEquals(1, stub.Scheduled.size());
        assertTrue(stub.Scheduled.get(0).isRecurring());
        stub.executeAllScheduled();
        assertEquals(1, stub.Scheduled.size());
    }


}

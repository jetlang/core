package org.jetlang.core;

/**
 * User: mrettig
 * Date: Jul 23, 2008
 * Time: 8:40:58 PM
 */
public class ThreadFiberTests extends FiberBaseTest {

    public ProcessFiber CreateBus() {
        return new ThreadFiber(new RunnableExecutorImpl(), System.currentTimeMillis() + "", true);
    }

    public void DoSetup() {
    }

    public void DoTearDown() {
    }


}

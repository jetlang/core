package org.jetlang;

import org.jetlang.core.RunnableInvoker;

public class PerfCommandExecutor implements RunnableInvoker {

    public void executeAll(Runnable[] toExecute) {
        for (Runnable runnable : toExecute) {
            runnable.run();
        }
        if (toExecute.length < 1000) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

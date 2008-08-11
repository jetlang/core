package org.jetlang;

import org.jetlang.core.RunnableInvoker;

import java.util.Collection;

public class PerfCommandExecutor implements RunnableInvoker {

    public void executeAll(Collection<Runnable> toExecute) {
        for (Runnable runnable : toExecute) {
            runnable.run();
        }
        if (toExecute.size() < 1000) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

package org.jetlang.core;

/**
 * Default implementation that simply executes all events.
 *
 * @author mrettig
 */
public class BatchExecutorImpl implements BatchExecutor {
    public void execute(Runnable[] toExecute) {
        for (int i = 0; i < toExecute.length; i++) {
            toExecute[i].run();
        }
    }
}

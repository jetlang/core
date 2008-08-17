package org.jetlang.core;

/**
 * User: mrettig
 * Date: Aug 16, 2008
 * Time: 11:01:28 AM
 */
public class BatchExecutorImpl implements BatchExecutor {
    public void execute(Runnable[] toExecute) {
        for (int i = 0; i < toExecute.length; i++) {
            toExecute[i].run();
        }
    }
}

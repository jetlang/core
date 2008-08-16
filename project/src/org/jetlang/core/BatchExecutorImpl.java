package org.jetlang.core;

import java.util.Collection;

/**
 * User: mrettig
 * Date: Aug 16, 2008
 * Time: 11:01:28 AM
 */
public class BatchExecutorImpl implements BatchExecutor {
    public void execute(Collection<Runnable> toExecute) {
        for (Runnable command : toExecute) {
            command.run();
        }
    }
}

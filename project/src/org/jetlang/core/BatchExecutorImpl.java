package org.jetlang.core;

import java.util.List;

/**
 * User: mrettig
 * Date: Aug 16, 2008
 * Time: 11:01:28 AM
 */
public class BatchExecutorImpl implements BatchExecutor {
    public void execute(List<Runnable> toExecute) {
        for (int i = 0; i < toExecute.size(); i++) {
            toExecute.get(i).run();
        }
    }
}

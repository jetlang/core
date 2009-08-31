package org.jetlang.core;

/**
 * Default implementation that simply executes all events.
 *
 * @author mrettig
 */
public class BatchExecutorImpl implements BatchExecutor {
    public void execute(EventReader toExecute) {
        for (int i = 0; i < toExecute.size(); i++) {
            toExecute.get(i).run();
        }
    }
}

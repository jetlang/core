package org.jetlang.core;

import java.util.Collection;
import java.util.concurrent.Executor;

/**
 * Helper functions for working with an {@link Executor}
 */
public class ExecutorHelper {

    /**
     * Execute all the supplied commands with the given Executor
     *
     * @param executor Executor to use for command execution
     * @param commands Commands to execute
     */
    public static void invokeAll(Executor executor, Collection<Runnable> commands) {
        for (Runnable command : commands) {
            executor.execute(command);
        }
    }
}

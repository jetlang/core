package org.jetlang.core;

/**
 * Event executor. Implementatations execute all Runnables. Typically, custom implementations
 * add logging and exception handling.
 *
 * @author mrettig
 */
public interface BatchExecutor {
    void execute(Runnable[] toExecute);
}

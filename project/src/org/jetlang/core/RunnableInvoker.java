package org.jetlang.core;

import java.util.Collection;

/// <summary>
/// Executes the pending events on for the process bus.

/// </summary>
public interface RunnableInvoker {
    /// <summary>
    /// Execute all pending events for the process bus.
    /// </summary>
    /// <param name="toExecute"></param>
    void executeAll(Collection<Runnable> toExecute);
}

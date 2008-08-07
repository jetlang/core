package org.jetlang.core;

import java.util.concurrent.Executor;

/// <summary>
/// Queue for command objects.

/// </summary>
public interface RunnableQueue extends Executor {

    void addOnStop(Disposable runOnStop);

    boolean removeOnStop(Disposable disposable);

    int stoppableSize();
}

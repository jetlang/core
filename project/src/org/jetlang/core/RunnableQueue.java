package org.jetlang.core;

import java.util.concurrent.Executor;

/**
 * Queue for runnable objects.
 */
public interface RunnableQueue extends Executor {

    void addOnStop(Disposable runOnStop);

    boolean removeOnStop(Disposable disposable);

    int registeredDisposableSize();
}

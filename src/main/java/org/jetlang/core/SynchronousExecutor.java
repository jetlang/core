package org.jetlang.core;

import java.util.concurrent.Executor;

/**
 * Executor that runs tasks on the current thread.
 */
public class SynchronousExecutor implements Executor {

    public boolean isRunning() {
        return _running;
    }

    public void setRunning(boolean _running) {
        this._running = _running;
    }

    private volatile boolean _running = true;

    public void execute(Runnable command) {
        if (_running) {
            command.run();
        }
    }

}

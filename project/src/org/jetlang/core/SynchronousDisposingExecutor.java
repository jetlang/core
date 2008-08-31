package org.jetlang.core;

import java.util.ArrayList;
import java.util.List;

/**
 * A synchronous execute typically used for testing.
 * <p/>
 * Events will be executed immediately, rather than queued and executed on another thread
 */
public class SynchronousDisposingExecutor implements RunnableExecutor {
    private boolean _running = true;
    private final List<Disposable> _onStop = new ArrayList<Disposable>();

    public void execute(Runnable command) {
        if (_running) {
            command.run();
        }
    }

    public void add(Disposable runOnStop) {
        _onStop.add(runOnStop);
    }

    public boolean remove(Disposable disposable) {
        return _onStop.remove(disposable);
    }

    public int size() {
        return _onStop.size();
    }

    public void run() {
        _running = true;
    }

    public void dispose() {
        _running = false;
        for (Disposable run : _onStop) {
            run.dispose();
        }
    }
}

package org.jetlang.core;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// A synchronous execute typically used for testing.

/// </summary>
public class SynchronousDisposingExecutor implements RunnableExecutor {
    private boolean _running = true;
    private List<Disposable> _onStop = new ArrayList<Disposable>();

    /// <summary>
    /// Queue command
    /// </summary>
    /// <param name="command"></param>
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

    /// <summary>
    /// start Consuming events.
    /// </summary>
    public void run() {
        _running = true;
    }

    /// <summary>
    /// Stop consuming events.
    /// </summary>
    public void dispose() {
        _running = false;
        for (Disposable run : _onStop) {
            run.dispose();
        }
    }
}

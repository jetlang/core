package org.jetlang.core;

class PendingCommand implements Disposable, Runnable {
    private final Runnable _toExecute;
    private boolean _cancelled;

    public PendingCommand(Runnable toExecute) {
        _toExecute = toExecute;
    }

    public void dispose() {
        _cancelled = true;
    }

    public void run() {
        if (!_cancelled) {
            _toExecute.run();
        }
    }
}

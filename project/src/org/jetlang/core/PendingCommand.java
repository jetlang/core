package org.jetlang.core;

class PendingCommand implements Stopable, Runnable {
    private final Runnable _toExecute;
    private boolean _cancelled;

    public PendingCommand(Runnable toExecute) {
        _toExecute = toExecute;
    }

    public void stop() {
        _cancelled = true;
    }

    public void run() {
        if (!_cancelled) {
            _toExecute.run();
        }
    }
}

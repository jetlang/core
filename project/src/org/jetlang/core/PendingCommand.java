package org.jetlang.core;

public class PendingCommand implements TimerControl {
    private final Runnable _toExecute;
    private boolean _cancelled;

    public PendingCommand(Runnable toExecute) {
        _toExecute = toExecute;
    }

    public void cancel() {
        _cancelled = true;
    }

    public void ExecuteCommand() {
        if (!_cancelled) {
            _toExecute.run();
        }
    }
}

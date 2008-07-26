package org.jetlang.core;

/// <summary>
/// Default command executor.

/// </summary>
public class CommandExecutor implements ICommandExecutor {
    private boolean _running = true;

    /// <summary>
    /// <see cref="ICommandExecutor.ExecuteAll(Command[])"/>
    /// </summary>
    /// <param name="toExecute"></param>
    public void executeAll(Runnable[] toExecute) {
        for (Runnable command : toExecute) {
            if (_running) {
                command.run();
            }
        }
    }

}

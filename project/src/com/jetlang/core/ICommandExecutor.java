package com.jetlang.core;

    /// <summary>
    /// Executes the pending events on for the process bus.
    /// </summary>
    public interface ICommandExecutor
    {
        /// <summary>
        /// Execute all pending events for the process bus.
        /// </summary>
        /// <param name="toExecute"></param>
        void executeAll(Runnable[] toExecute);
    }

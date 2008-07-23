package com.jetlang.core;

    /// <summary>
    /// A runable queue implementation.
    /// </summary>
    public interface ICommandRunner extends ICommandQueue
    {
        /// <summary>
        /// Consume events.
        /// </summary>
        void Run();

        /// <summary>
        /// Stop consuming events.
        /// </summary>
        void Stop();
    }

package com.jetlang.core;

public interface IProcessQueue extends ICommandQueue, ICommandTimer, IDisposable
    {
        /// <summary>
        /// Start consuming events.
        /// </summary>
        void Start();

        /// <summary>
        /// Stop consuming events.
        /// </summary>
        void Stop();
    }

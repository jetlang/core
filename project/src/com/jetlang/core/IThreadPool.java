package com.jetlang.core;

    /// <summary>
    /// A thread pool for executing asynchronous events.
    /// </summary>
    public interface IThreadPool
    {
        /// <summary>
        /// Queue event for execution.
        /// </summary>
        /// <param name="callback"></param>
        void Queue(Runnable callback);
    }


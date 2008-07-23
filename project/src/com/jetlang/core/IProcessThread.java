package com.jetlang.core;

public interface IProcessThread extends IProcessQueue
    {
        /// <summary>
        /// The backing thead.
        /// </summary>
        Thread getThread();

        /// <summary>
        /// Wait for the thread to complete.
        /// </summary>
        void Join();
    }

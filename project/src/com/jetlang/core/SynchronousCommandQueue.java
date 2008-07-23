package com.jetlang.core;

    /// <summary>
    /// A synchronous queue typically used for testing.
    /// </summary>
    public class SynchronousCommandQueue implements ICommandQueue, ICommandRunner
    {
        private boolean _running = true;

        /// <summary>
        /// Queue command
        /// </summary>
        /// <param name="command"></param>
        public void queue(Runnable command)
        {
            if (_running)
            {
                command.run();
            }
        }

        /// <summary>
        /// Start Consuming events.
        /// </summary>
        public void Run()
        {
            _running = true;
        }

        /// <summary>
        /// Stop consuming events.
        /// </summary>
        public void Stop()
        {
            _running = false;
        }
    }

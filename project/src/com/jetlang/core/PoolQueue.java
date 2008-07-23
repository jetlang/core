package com.jetlang.core;

import java.util.ArrayList;
import java.util.List;

/// <summary>
    /// Process Queue that uses a thread pool for execution.
    /// </summary>
    public class PoolQueue
    {
        private boolean _flushPending = false;
        private final Object _lock = new Object();
        private final List<Runnable> _queue = new ArrayList<Runnable>();
        private final IThreadPool _pool;
        private ExecutionState _started = ExecutionState.Created;
        private final ICommandExecutor _executor;

        /// <summary>
        /// Construct new instance.
        /// </summary>
        /// <param name="pool"></param>
        /// <param name="executor"></param>
        public PoolQueue(IThreadPool pool, ICommandExecutor executor)
        {
            _pool = pool;
            _executor = executor;
        }

        /// <summary>
        /// Queue command.
        /// </summary>
        /// <param name="commands"></param>
        public void Enqueue(Runnable commands)
        {
            if (_started == ExecutionState.Stopped)
            {
                return;
            }

            synchronized (_lock)
            {
                _queue.add(commands);
                if (_started == ExecutionState.Created)
                {
                    return;
                }
                if (!_flushPending)
                {
                    Runnable flushRunnable = new Runnable(){
                        public void run() {
                            Flush();
                        }
                    };
                    _pool.Queue(flushRunnable);
                    _flushPending = true;
                }
            }
        }

        private void Flush()
        {
            Runnable[] toExecute = ClearCommands();
            if (toExecute != null)
            {
                _executor.executeAll(toExecute);
                synchronized (_lock)
                {
                    if (_queue.size() > 0)
                    {
                        // don't monopolize thread.
                        Runnable flushRunnable = new Runnable(){
                        public void run() {
                            Flush();
                        }
                        };
                        _pool.Queue(flushRunnable);
                    }
                    else
                    {
                        _flushPending = false;
                    }
                }
            }
        }

        private Runnable[] ClearCommands()
        {
            synchronized (_lock)
            {
                if (_queue.size() == 0)
                {
                    _flushPending = false;
                    return null;
                }
                Runnable[] toReturn = _queue.toArray(new Runnable[_queue.size()]);
                _queue.clear();
                return toReturn;
            }
        }

        /// <summary>
        /// <see cref="ICommandTimer.Schedule(Command,long)"/>
        /// </summary>
        /// <param name="command"></param>
        /// <param name="firstIntervalInMs"></param>
        /// <returns></returns>
//        public ITimerControl Schedule(Runnable command, long firstIntervalInMs)
//        {
//            return _timer.Schedule(command, firstIntervalInMs);
//        }

        /// <summary>
        /// <see cref="ICommandTimer.ScheduleOnInterval(Command,long,long)"/>
        /// </summary>
        /// <param name="command"></param>
        /// <param name="firstIntervalInMs"></param>
        /// <param name="regularIntervalInMs"></param>
        /// <returns></returns>
//        public ITimerControl ScheduleOnInterval(Runnable command, long firstIntervalInMs, long regularIntervalInMs)
//        {
//            return _timer.ScheduleOnInterval(command, firstIntervalInMs, regularIntervalInMs);
//        }

        /// <summary>
        /// Start consuming events.
        /// </summary>
        public void Start()
        {
            if (_started == ExecutionState.Running)
            {
                throw new RuntimeException("Already Started");
            }
            _started = ExecutionState.Running;
            //flush any pending events in queue
            Runnable flushPending = new Runnable(){
                public void run() {

                }
            };
            Enqueue(flushPending);
        }

        /// <summary>
        /// Stop consuming events.
        /// </summary>
        public void Stop()
        {
            //_timer.Dispose();
            _started = ExecutionState.Stopped;
        }

        /// <summary>
        /// Stops the queue.
        /// </summary>
        public void Dispose()
        {
            Stop();
        }
    }

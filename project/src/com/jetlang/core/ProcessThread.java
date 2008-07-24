package com.jetlang.core;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// Default implementation for IProcessThread.
/// <see cref="IProcessThread"/>

/// </summary>
public class ProcessThread implements IProcessThread {

    private final Thread _thread;
    private final ICommandRunner _queue;
    private final List<Runnable> _onStop = new ArrayList<Runnable>();
    //private final CommandTimer _scheduler;

    /// <summary>
    /// Create process thread.
    /// </summary>
    /// <param name="queue"></param>
    /// <param name="threadName"></param>
    /// <param name="isBackground"></param>

    public ProcessThread(ICommandRunner queue, String threadName, boolean isBackground) {
        _queue = queue;
        Runnable runThread = new Runnable() {
            public void run() {
                RunThread();
            }
        };
        _thread = new Thread(runThread, threadName);
        _thread.setDaemon(!isBackground);
        //_scheduler = new CommandTimer(this);
    }

    /// <summary>
    /// <see cref="IProcessThread.Thread"/>
    /// </summary>
    public Thread getThread() {
        return _thread;
    }

    private void RunThread() {
        _queue.Run();
    }

    /// <summary>
    /// Queue command.
    /// </summary>
    /// <param name="command"></param>
    public void queue(Runnable command) {
        _queue.queue(command);
    }

    public void onStop(Runnable runOnStop) {
        synchronized (_onStop) {
            _onStop.add(runOnStop);
        }
    }

    /// <summary>
    /// <see cref="ICommandTimer.Schedule(Command,long)"/>
    /// </summary>
    /// <param name="command"></param>
    /// <param name="intervalInMs"></param>
    /// <returns></returns>
//        public ITimerControl Schedule(Command command, long intervalInMs)
//        {
//            //return _scheduler.Schedule(command, intervalInMs);
//        }

    /// <summary>
    /// <see cref="ICommandTimer.ScheduleOnInterval(Command,long,long)"/>
    /// </summary>
    /// <param name="command"></param>
    /// <param name="firstInMs"></param>
    /// <param name="intervalInMs"></param>
//        public ITimerControl ScheduleOnInterval(Command command, long firstInMs, long intervalInMs)
//        {
//            return _scheduler.ScheduleOnInterval(command, firstInMs, intervalInMs);
//        }

    /// <summary>
    /// <see cref="IProcessQueue.Stop"/>
    /// </summary>

    public void Stop() {
//            _scheduler.Dispose();
        _queue.Stop();
        synchronized (_onStop) {
            for (Runnable r : _onStop) {
                r.run();
            }
        }
    }

    /// <summary>
    /// <see cref="IProcessQueue.Start"/>
    /// </summary>
    public void Start() {
        _thread.start();
    }

    /// <summary>
    /// <see cref="IProcessThread.Join"/>
    /// </summary>
    public void Join() {
        try {
            _thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /// <summary>
    /// Stops the thread.
    /// </summary>
    public void Dispose() {
        Stop();
    }
}

package org.jetlang.core;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// Default implementation for ThreadFiber.
/// <see cref="ThreadFiber"/>

/// </summary>
public class ThreadFiber implements ProcessFiber {

    private final Thread _thread;
    private final ICommandRunner _queue;
    private final List<Runnable> _onStop = new ArrayList<Runnable>();
    private final CommandTimer _scheduler;

    /// <summary>
    /// Create process thread.
    /// </summary>
    /// <param name="queue"></param>
    /// <param name="threadName"></param>
    /// <param name="isBackground"></param>

    public ThreadFiber(ICommandRunner queue, String threadName, boolean isBackground) {
        _queue = queue;
        Runnable runThread = new Runnable() {
            public void run() {
                RunThread();
            }
        };
        _thread = new Thread(runThread, threadName);
        _thread.setDaemon(!isBackground);
        _scheduler = new CommandTimer(this);
    }

    /// <summary>
    /// <see cref="ThreadFiber.Thread"/>
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
    /// <see cref="RunnableScheduler.Schedule(Command,long)"/>
    /// </summary>
    /// <param name="command"></param>
    /// <param name="intervalInMs"></param>
    /// <returns></returns>
//        public TimerControl Schedule(Command command, long intervalInMs)
//        {
//            //return _scheduler.Schedule(command, intervalInMs);
//        }

    /// <summary>
    /// <see cref="RunnableScheduler.scheduleOnInterval(Command,long,long)"/>
    /// </summary>
    /// <param name="command"></param>
    /// <param name="firstInMs"></param>
    /// <param name="intervalInMs"></param>
//        public TimerControl scheduleOnInterval(Command command, long firstInMs, long intervalInMs)
//        {
//            return _scheduler.scheduleOnInterval(command, firstInMs, intervalInMs);
//        }

    /// <summary>
    /// <see cref="ProcessFiber.Stop"/>
    /// </summary>

    public void stop() {
//            _scheduler.stop();
        _queue.Stop();
        synchronized (_onStop) {
            for (Runnable r : _onStop) {
                r.run();
            }
        }
    }

    /// <summary>
    /// <see cref="ProcessFiber.start"/>
    /// </summary>
    public void start() {
        _thread.start();
    }

    /// <summary>
    /// <see cref="ThreadFiber.Join"/>
    /// </summary>
    public void Join() {
        try {
            _thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /// <summary>
    /// Schedules an event to be executes once.
    /// </summary>
    /// <param name="command"></param>
    /// <param name="firstIntervalInMs"></param>
    /// <returns>a controller to cancel the event.</returns>
    public TimerControl schedule(Runnable command, long firstIntervalInMs) {
        return _scheduler.schedule(command, firstIntervalInMs);
    }/// <summary>

    /// Schedule an event on a recurring interval.
    /// </summary>
    /// <param name="command"></param>
    /// <param name="firstIntervalInMs"></param>
    /// <param name="regularIntervalInMs"></param>
    /// <returns>controller to cancel timer.</returns>
    public TimerControl scheduleOnInterval(Runnable command, long firstIntervalInMs, long regularIntervalInMs) {
        return _scheduler.scheduleOnInterval(command, firstIntervalInMs, regularIntervalInMs);
    }
}

package org.jetlang.core;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// Default implementation for ThreadFiber.
/// <see cref="ThreadFiber"/>

/// </summary>
public class ThreadFiber implements ProcessFiber {

    private final Thread _thread;
    private final RunnableExecutor _queue;
    private final List<Stopable> _onStop = new ArrayList<Stopable>();
    private final RunnableSchedulerImpl _scheduler;

    /// <summary>
    /// Create process thread.
    /// </summary>
    /// <param name="execute"></param>
    /// <param name="threadName"></param>
    /// <param name="isBackground"></param>

    public ThreadFiber(RunnableExecutor queue, String threadName, boolean isDaemonThread) {
        _queue = queue;
        Runnable runThread = new Runnable() {
            public void run() {
                RunThread();
            }
        };
        _thread = new Thread(runThread, threadName);
        _thread.setDaemon(isDaemonThread);
        _scheduler = new RunnableSchedulerImpl(this);
    }

    /// <summary>
    /// <see cref="ThreadFiber.Thread"/>
    /// </summary>
    public Thread getThread() {
        return _thread;
    }

    private void RunThread() {
        _queue.run();
    }

    /// <summary>
    /// Queue command.
    /// </summary>
    /// <param name="command"></param>
    public void execute(Runnable command) {
        _queue.execute(command);
    }

    public void onStop(Stopable runOnStop) {
        synchronized (_onStop) {
            _onStop.add(runOnStop);
        }
    }

    public boolean removeOnStop(Stopable stopable) {
        synchronized (_onStop) {
            return _onStop.remove(stopable);
        }
    }

    /// <summary>
    /// <see cref="ProcessFiber.Stop"/>
    /// </summary>

    public void stop() {
        _scheduler.stop();
        _queue.stop();
        synchronized (_onStop) {
            for (Stopable r : _onStop.toArray(new Stopable[_onStop.size()])) {
                r.stop();
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
    /// <see cref="ThreadFiber.join"/>
    /// </summary>
    public void join() {
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
    /// <returns>a controller to stop the event.</returns>
    public Stopable schedule(Runnable command, long firstIntervalInMs) {
        return _scheduler.schedule(command, firstIntervalInMs);
    }/// <summary>

    /// Schedule an event on a recurring interval.
    /// </summary>
    /// <param name="command"></param>
    /// <param name="firstIntervalInMs"></param>
    /// <param name="regularIntervalInMs"></param>
    /// <returns>controller to stop timer.</returns>
    public Stopable scheduleOnInterval(Runnable command, long firstIntervalInMs, long regularIntervalInMs) {
        return _scheduler.scheduleOnInterval(command, firstIntervalInMs, regularIntervalInMs);
    }
}

package org.jetlang.fibers;

import org.jetlang.core.Disposable;
import org.jetlang.core.RunnableExecutor;
import org.jetlang.core.RunnableExecutorImpl;
import org.jetlang.core.SchedulerImpl;

/// <summary>
/// Default implementation for ThreadFiber.
/// <see cref="ThreadFiber"/>

/// </summary>
public class ThreadFiber implements Fiber {

    private final Thread _thread;
    private final RunnableExecutor _queue;
    private final Scheduler _scheduler;

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
        _thread = createThread(threadName, runThread);
        _thread.setDaemon(isDaemonThread);
        _scheduler = new SchedulerImpl(queue);
    }

    private Thread createThread(String threadName, Runnable runThread) {
        if (threadName == null) {
            return new Thread(runThread);
        }
        return new Thread(runThread, threadName);
    }

    public ThreadFiber() {
        this(new RunnableExecutorImpl(), null, true);
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

    public void add(Disposable runOnStop) {
        _queue.add(runOnStop);
    }

    public boolean remove(Disposable disposable) {
        return _queue.remove(disposable);
    }

    public int size() {
        return _queue.size();
    }

    /// <summary>
    /// <see cref="Fiber.Stop"/>
    /// </summary>

    public void dispose() {
        _scheduler.dispose();
        _queue.dispose();
    }

    /// <summary>
    /// <see cref="Fiber.start"/>
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
    /// <returns>a controller to dispose the event.</returns>
    public Disposable schedule(Runnable command, long firstIntervalInMs) {
        return _scheduler.schedule(command, firstIntervalInMs);
    }/// <summary>

    /// Schedule an event on a recurring interval.
    /// </summary>
    /// <param name="command"></param>
    /// <param name="firstIntervalInMs"></param>
    /// <param name="regularIntervalInMs"></param>
    /// <returns>controller to dispose timer.</returns>
    public Disposable scheduleOnInterval(Runnable command, long firstIntervalInMs, long regularIntervalInMs) {
        return _scheduler.scheduleOnInterval(command, firstIntervalInMs, regularIntervalInMs);
    }
}

package org.jetlang.core;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

public class RunnableSchedulerImpl implements RunnableScheduler {
    private final Timer _timer;
    private final Executor _queue;

    public RunnableSchedulerImpl(Executor queue) {
        _queue = queue;
        _timer = new Timer(true);
    }

    public RunnableSchedulerImpl(RunnableQueue queue, Timer timer) {
        _queue = queue;
        _timer = timer;
    }

    public Disposable schedule(final Runnable comm, long timeTillEnqueueInMs) {
        if (timeTillEnqueueInMs <= 0) {
            PendingCommand pending = new PendingCommand(comm);
            _queue.execute(pending);
            return pending;
        } else {
            TimerTask task = new TimerTask() {
                public void run() {
                    _queue.execute(comm);
                }
            };
            _timer.schedule(task, timeTillEnqueueInMs);
            return new TimerTaskControl(task);
        }
    }

    public Disposable scheduleOnInterval(final Runnable comm, long firstInMs, long intervalInMs) {
        TimerTask task = new TimerTask() {
            public void run() {
                _queue.execute(comm);
            }
        };
        _timer.schedule(task, firstInMs, intervalInMs);
        return new TimerTaskControl(task);
    }

    public void dispose() {
        _timer.cancel();
    }
}

class TimerTaskControl implements Disposable {
    private TimerTask _task;

    public TimerTaskControl(TimerTask task) {
        _task = task;
    }

    /// <summary>
    /// Cancels scheduled timer.
    /// </summary>
    public void dispose() {
        _task.cancel();
    }
}
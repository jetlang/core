package org.jetlang.core;

import java.util.Timer;
import java.util.TimerTask;

public class RunnableSchedulerImpl implements RunnableScheduler, Stopable {
    private final Timer _timer;
    private final RunnableQueue _queue;

    public RunnableSchedulerImpl(RunnableQueue queue) {
        _queue = queue;
        _timer = new Timer(true);
    }

    public RunnableSchedulerImpl(RunnableQueue queue, Timer timer) {
        _queue = queue;
        _timer = timer;
    }

    public Stopable schedule(final Runnable comm, long timeTillEnqueueInMs) {
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

    public Stopable scheduleOnInterval(final Runnable comm, long firstInMs, long intervalInMs) {
        TimerTask task = new TimerTask() {
            public void run() {
                _queue.execute(comm);
            }
        };
        _timer.schedule(task, firstInMs, intervalInMs);
        return new TimerTaskControl(task);
    }

    public void stop() {
        _timer.cancel();
    }
}

class TimerTaskControl implements Stopable {
    private TimerTask _task;

    public TimerTaskControl(TimerTask task) {
        _task = task;
    }

    /// <summary>
    /// Cancels scheduled timer.
    /// </summary>
    public void stop() {
        _task.cancel();
    }
}
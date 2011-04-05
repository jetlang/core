package org.jetlang.core;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default implementation for scheduling events for execution on fibers.
 */
public class SchedulerImpl implements Scheduler {

    private final ScheduledExecutorService _scheduler;
    private final Executor _queue;

    public SchedulerImpl(Executor queue) {
        _queue = queue;
        _scheduler = createSchedulerThatIgnoresEventsAfterStop();
    }

    public static ScheduledThreadPoolExecutor createSchedulerThatIgnoresEventsAfterStop() {
        ThreadFactory fact = new DaemonThreadFactory();
        ScheduledThreadPoolExecutor s = new ScheduledThreadPoolExecutor(1, fact);
        RejectedExecutionHandler handler = new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                if (!executor.isShutdown()) {
                    throw new RejectedExecutionException("Rejected Execution: " + r);
                }
                //ignore tasks if shutdown already.
            }
        };
        s.setRejectedExecutionHandler(handler);
        return s;
    }

    public SchedulerImpl(DisposingExecutor queue, ScheduledExecutorService scheduler) {
        _queue = queue;
        _scheduler = scheduler;
    }

    public Disposable schedule(Runnable _command, long delay, TimeUnit unit) {
        if (delay == 0) {
            PendingCommand c = new PendingCommand(_command);
            _queue.execute(c);
            return c;
        } else {
            PendingCommand command = new PendingCommand(_command);
            return new ScheduledFutureControl(_scheduler.schedule(new ExecuteCommand(command), delay, unit), command);
        }
    }

    public Disposable scheduleAtFixedRate(Runnable _command, long initialDelay, long interval, TimeUnit unit) {
        PendingCommand command = new PendingCommand(_command);
        return new ScheduledFutureControl(
                _scheduler.scheduleAtFixedRate(new ExecuteCommand(command), initialDelay, interval, unit),
                command);
    }

    private class FixedDelayTask implements Runnable, Disposable {
        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private final Runnable target;
        private final long interval;
        private final TimeUnit unit;
        private volatile Disposable scheduledEvent;

        public FixedDelayTask(Runnable target, long interval, TimeUnit unit) {
            this.target = target;
            this.interval = interval;
            this.unit = unit;
        }

        public void dispose() {
            if (cancelled.compareAndSet(false, true)) {
                scheduledEvent.dispose();
            }
        }

        public void run() {
            if (cancelled.get()) return;
            try {
                target.run();
            } finally {
                if (!cancelled.get()) {
                    scheduledEvent = schedule(this, interval, unit);
                }
            }
        }
    }

    public Disposable scheduleWithFixedDelay(final Runnable command, long initialDelay, long interval, TimeUnit unit) {
        FixedDelayTask fixedDelayTask = new FixedDelayTask(command, interval, unit);
        fixedDelayTask.scheduledEvent = schedule(fixedDelayTask, initialDelay, unit);
        return fixedDelayTask;
    }

    public void dispose() {
        _scheduler.shutdown();
    }


    private class ExecuteCommand implements Runnable {
        private final Runnable command;

        public ExecuteCommand(Runnable command) {
            this.command = command;
        }

        public void run() {
            _queue.execute(command);
        }
    }

    private static class PendingCommand implements Disposable, Runnable {
        private final Runnable _toExecute;
        private volatile boolean _cancelled;

        public PendingCommand(Runnable toExecute) {
            _toExecute = toExecute;
        }

        public void dispose() {
            _cancelled = true;
        }

        public void run() {
            if (!_cancelled) {
                _toExecute.run();
            }
        }

        @Override
        public String toString() {
            return _toExecute.toString();
        }
    }

    private final class ScheduledFutureControl implements Disposable {
        private final ScheduledFuture<?> future;
        private final Disposable command;

        public ScheduledFutureControl(ScheduledFuture<?> future, Disposable command) {
            this.future = future;
            this.command = command;
        }

        public void dispose() {
            command.dispose();
            future.cancel(false);
        }
    }
}


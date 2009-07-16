package org.jetlang.core;

import java.util.concurrent.*;

/**
 * Default implementation for scheduling events for execution on fibers.
 */
public class SchedulerImpl implements Scheduler {
    private final ScheduledExecutorService _scheduler;
    private final Executor _queue;

    public SchedulerImpl(Executor queue) {
        _queue = queue;
        ThreadFactory fact = new DaemonThreadFactory();
        _scheduler = Executors.newSingleThreadScheduledExecutor(fact);
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

    public Disposable scheduleWithFixedDelay(Runnable _command, long initialDelay, long interval, TimeUnit unit) {
        PendingCommand command = new PendingCommand(_command);
        return new ScheduledFutureControl(
                _scheduler.scheduleWithFixedDelay(new ExecuteCommand(command), initialDelay, interval, unit),
                command);
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


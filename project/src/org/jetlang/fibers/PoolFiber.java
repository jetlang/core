package org.jetlang.fibers;

import org.jetlang.core.Disposable;
import static org.jetlang.core.ExecutorHelper.invokeAll;
import org.jetlang.core.SchedulerImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/// <summary>
/// Process Queue that uses a thread pool for execution.

/// </summary>
class PoolFiber implements Fiber {
    private final AtomicBoolean _flushPending = new AtomicBoolean(false);
    private final BlockingQueue<Runnable> _queue = new ArrayBlockingQueue<Runnable>(1000);
    private final Executor _flushExecutor;
    private final AtomicReference<ExecutionState> _started = new AtomicReference<ExecutionState>(ExecutionState.Created);
    private final Executor _commandExecutor;
    private final Collection<Disposable> _disposables = Collections.synchronizedList(new ArrayList<Disposable>());
    private final SchedulerImpl _scheduler;
    private final Runnable _flushRunnable;

    /// <summary>
    /// Construct new instance.
    /// </summary>
    /// <param name="pool"></param>
    /// <param name="executor"></param>
    PoolFiber(Executor pool, Executor executor, ScheduledExecutorService scheduler) {
        _flushExecutor = pool;
        _commandExecutor = executor;
        _scheduler = new SchedulerImpl(this, scheduler);
        _flushRunnable = new Runnable() {
            public void run() {
                flush();
            }
        };
    }

    /// <summary>
    /// Queue command.
    /// </summary>
    /// <param name="commands"></param>
    public void execute(Runnable commands) {
        if (_started.get() == ExecutionState.Stopped) {
            return;
        }

        _queue.add(commands);

        if (_started.get() == ExecutionState.Created) {
            return;
        }

        flushIfNotPending();
    }

    private void flushIfNotPending() {
        if (_flushPending.compareAndSet(false, true)) {
            _flushExecutor.execute(_flushRunnable);
        }
    }

    private void flush() {
        invokeAll(_commandExecutor, flushPendingCommands());

        _flushPending.compareAndSet(true, false);

        if (!_queue.isEmpty()) {
            flushIfNotPending();
        }
    }

    private List<Runnable> flushPendingCommands() {
        List<Runnable> commands = new ArrayList<Runnable>();

        _queue.drainTo(commands);

        return commands;
    }

    public void start() {
        ExecutionState state = _started.get();

        if (state == ExecutionState.Running) {
            throw new RuntimeException("Already Started");
        }

        if (_started.compareAndSet(state, ExecutionState.Running)) {
            execute(new Runnable() {
                public void run() {
                    //flush any pending events in execute
                }
            });
        }
    }

    public void dispose() {
        _started.set(ExecutionState.Stopped);
        synchronized (_disposables) {
            for (Disposable r : _disposables) {
                r.dispose();
            }
        }
    }

    public void add(Disposable runOnStop) {
        _disposables.add(runOnStop);
    }

    public boolean remove(Disposable disposable) {
        return _disposables.remove(disposable);
    }

    public int size() {
        return _disposables.size();
    }

    /// <summary>
    /// Schedules an event to be executes once.
    /// </summary>
    /// <param name="command"></param>
    /// <param name="firstIntervalInMs"></param>
    /// <returns>a controller to dispose the event.</returns>
    public Disposable schedule(Runnable command, long delay, TimeUnit unit) {
        return _scheduler.schedule(command, delay, TimeUnit.MILLISECONDS);
    }

    /// Schedule an event on a recurring interval.
    /// </summary>
    /// <param name="command"></param>
    /// <param name="firstIntervalInMs"></param>
    /// <param name="regularIntervalInMs"></param>
    /// <returns>controller to dispose timer.</returns>
    public Disposable scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        //the timer object is shared so interval timers must be shut down manually.
        final Disposable stopper = _scheduler.scheduleWithFixedDelay(command, initialDelay, delay, TimeUnit.MILLISECONDS);
        Disposable wrapper = new Disposable() {
            public void dispose() {
                stopper.dispose();
                remove(this);
            }
        };
        add(wrapper);
        return wrapper;
    }
}

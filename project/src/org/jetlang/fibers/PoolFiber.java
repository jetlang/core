package org.jetlang.fibers;

import org.jetlang.core.Disposable;
import org.jetlang.core.RunnableInvoker;
import org.jetlang.core.RunnableSchedulerImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/// <summary>
/// Process Queue that uses a thread pool for execution.

/// </summary>
public class PoolFiber implements Fiber {
    private final AtomicBoolean _flushPending = new AtomicBoolean(false);
    private final BlockingQueue<Runnable> _queue = new ArrayBlockingQueue<Runnable>(1000);
    private final Executor _pool;
    private final AtomicReference<ExecutionState> _started = new AtomicReference<ExecutionState>(ExecutionState.Created);
    private final RunnableInvoker _executor;
    private final Collection<Disposable> _onStop = new ArrayList<Disposable>();
    private final RunnableSchedulerImpl _scheduler;
    private final Runnable _flushRunnable;

    /// <summary>
    /// Construct new instance.
    /// </summary>
    /// <param name="pool"></param>
    /// <param name="executor"></param>
    public PoolFiber(Executor pool, RunnableInvoker executor, Timer timer) {
        _pool = pool;
        _executor = executor;
        _scheduler = new RunnableSchedulerImpl(this, timer);
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
            _pool.execute(_flushRunnable);
        }
    }

    private void flush() {
        _executor.executeAll(ClearCommands());

        _flushPending.compareAndSet(true, false);

        if (!_queue.isEmpty()) {
            flushIfNotPending();
        }
    }

    private List<Runnable> ClearCommands() {
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
            //flush any pending events in execute
            Runnable flushPending = new Runnable() {
                public void run() {
                }
            };
            execute(flushPending);
        }
    }


    /// <summary>
    /// Stop consuming events.
    /// </summary>
    public void dispose() {
        _started.set(ExecutionState.Stopped);
        synchronized (_onStop) {
            for (Disposable r : _onStop.toArray(new Disposable[_onStop.size()]))
                r.dispose();
        }
    }

    public void add(Disposable runOnStop) {
        synchronized (_onStop) {
            _onStop.add(runOnStop);
        }
    }

    public boolean remove(Disposable disposable) {
        synchronized (_onStop) {
            return _onStop.remove(disposable);
        }
    }

    public int size() {
        synchronized (_onStop) {
            return _onStop.size();
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
        //the timer object is shared so interval timers must be shut down manually.
        final Disposable stopper = _scheduler.scheduleOnInterval(command, firstIntervalInMs, regularIntervalInMs);
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

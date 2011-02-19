package org.jetlang.fibers;

import org.jetlang.core.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Fiber that uses a thread pool for execution.
 *
 * @author mrettig
 */
class PoolFiber implements Fiber {
    private final AtomicBoolean _flushPending = new AtomicBoolean(false);
    private final RunnableBlockingQueue _queue = new RunnableBlockingQueue();
    private final Executor _flushExecutor;
    private final AtomicReference<ExecutionState> _started = new AtomicReference<ExecutionState>(ExecutionState.Created);
    private final BatchExecutor _commandExecutor;
    private final Collection<Disposable> _disposables = Collections.synchronizedList(new ArrayList<Disposable>());
    private final SchedulerImpl _scheduler;
    private final Runnable _flushRunnable;
    private EventBuffer buffer = new EventBuffer();

    public PoolFiber(Executor pool, BatchExecutor executor, ScheduledExecutorService scheduler) {
        _flushExecutor = pool;
        _commandExecutor = executor;
        _scheduler = new SchedulerImpl(this, scheduler);
        _flushRunnable = new Runnable() {
            public void run() {
                flush();
            }
        };
    }

    public void execute(Runnable commands) {
        if (_started.get() == ExecutionState.Stopped) {
            return;
        }

        _queue.put(commands);

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
        buffer = _queue.swap(buffer);
        _commandExecutor.execute(buffer);
        buffer.clear();

        _flushPending.compareAndSet(true, false);

        if (!_queue.isEmpty()) {
            flushIfNotPending();
        }
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
            //copy list to prevent concurrent mod
            for (Disposable r : _disposables.toArray(new Disposable[_disposables.size()])) {
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

    public Disposable schedule(Runnable command, long delay, TimeUnit unit) {
        return _scheduler.schedule(command, delay, unit);
    }

    public Disposable scheduleAtFixedRate(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        //the timer object is shared so interval timers must be shut down manually.
        final Disposable stopper = _scheduler.scheduleAtFixedRate(command, initialDelay, delay, unit);
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

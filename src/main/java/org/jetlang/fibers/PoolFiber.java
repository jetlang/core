package org.jetlang.fibers;

import org.jetlang.core.BatchExecutor;
import org.jetlang.core.Disposable;
import org.jetlang.core.EventBuffer;
import org.jetlang.core.SchedulerImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Fiber that uses a thread pool for execution.
 *
 * @author mrettig
 */
class PoolFiber implements Fiber {

    private final Queue _queue = new Queue();
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

    private class Queue {
        private boolean running = true;
        private boolean flushPending = false;
        private EventBuffer _queue = new EventBuffer();

        public synchronized void setRunning(boolean newValue) {
            running = newValue;
        }

        public synchronized void put(Runnable r) {
            _queue.add(r);
            if (running && !flushPending) {
                _flushExecutor.execute(_flushRunnable);
                flushPending = true;
            }
        }

        public synchronized EventBuffer swap(EventBuffer buffer) {
            if (_queue.isEmpty()) {
                flushPending = false;
                return null;
            }
            EventBuffer toReturn = _queue;
            _queue = buffer;
            return toReturn;
        }
    }

    public void execute(Runnable commands) {
        if (_started.get() == ExecutionState.Stopped) {
            return;
        }
        _queue.put(commands);
    }

    private void flush() {
        EventBuffer swap = _queue.swap(buffer);
        while (swap != null) {
            buffer = swap;
            _commandExecutor.execute(buffer);
            buffer.clear();
            swap = _queue.swap(buffer);
        }
    }

    public void start() {
        ExecutionState state = _started.get();

        if (state == ExecutionState.Running) {
            throw new RuntimeException("Already Started");
        }

        if (_started.compareAndSet(state, ExecutionState.Running)) {
            _queue.setRunning(true);
            execute(new Runnable() {
                public void run() {
                    //flush any pending events in execute
                }
            });
        }
    }

    public void dispose() {
        _queue.setRunning(false);
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
        return register(_scheduler.scheduleAtFixedRate(command, initialDelay, delay, unit));
    }

    public Disposable scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return register(_scheduler.scheduleWithFixedDelay(command, initialDelay, delay, unit));
    }

    private Disposable register(final Disposable stopper) {
        Disposable wrapper = new Disposable() {
            public void dispose() {
                stopper.dispose();
                remove(this);
            }
        };
        //scheduler is shared so tasks removed individually
        add(wrapper);
        return wrapper;

    }
}

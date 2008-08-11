package org.jetlang.core;

import static org.jetlang.core.ExecutorHelper.invokeAll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

/// <summary>
/// Default implementation.

/// </summary>
public class RunnableExecutorImpl implements RunnableExecutor {
    private final Object _lock = new Object();
    private volatile boolean _running = true;

    // TODO the ArrayBlockingQueue is generally a tad faster, but its capacity-limited..
    private final BlockingQueue<Runnable> _commands = new LinkedBlockingQueue<Runnable>();
    private final List<Disposable> _disposables = new ArrayList<Disposable>();

    private final Executor _commandExecutor;

    public RunnableExecutorImpl() {
        this(new SynchronousExecutor());
    }

    public RunnableExecutorImpl(Executor executor) {
        _commandExecutor = executor;
    }

    public void execute(Runnable command) {
        _commands.add(command);
    }

    /// <summary>
    /// Remove all commands.
    /// </summary>
    /// <returns></returns>
    private Collection<Runnable> dequeueAll() {
        List<Runnable> dequeued = new ArrayList<Runnable>();
        Runnable command;

        try {
            command = _commands.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        dequeued.add(command);
        _commands.drainTo(dequeued);

        return dequeued;
    }

    public void run() {
        while (_running) {
            invokeAll(_commandExecutor, dequeueAll());
        }
    }

    public void dispose() {
        _running = false;

        execute(new Runnable() {
            public void run() {
                // so it wakes up and will notice that we've told it to stop
            }
        });

        synchronized (_lock) {
            for (Disposable r : _disposables.toArray(new Disposable[_disposables.size()])) {
                r.dispose();
            }
        }
    }

    public void add(Disposable r) {
        synchronized (_lock) {
            _disposables.add(r);
        }
    }

    public boolean remove(Disposable disposable) {
        synchronized (_lock) {
            return _disposables.remove(disposable);
        }
    }

    public int size() {
        synchronized (_lock) {
            return _disposables.size();
        }
    }
}

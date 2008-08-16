package org.jetlang.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/// <summary>
/// Default implementation.

/// </summary>
public class RunnableExecutorImpl implements RunnableExecutor {
    private volatile boolean _running = true;

    private final List<Runnable> _commands = new ArrayList<Runnable>();
    private final List<Disposable> _disposables = Collections.synchronizedList(new ArrayList<Disposable>());

    private final BatchExecutor _commandExecutor;

    public RunnableExecutorImpl() {
        this(new BatchExecutorImpl());
    }

    public RunnableExecutorImpl(BatchExecutor executor) {
        _commandExecutor = executor;
    }

    public void execute(Runnable command) {
        synchronized (_commands) {
            _commands.add(command);
        }
    }

    private Collection<Runnable> dequeueAll() {
        synchronized (_commands) {
            List<Runnable> dequeued = new ArrayList<Runnable>(_commands);
            _commands.clear();
            return dequeued;
        }
    }

    public void run() {
        while (_running) {
            _commandExecutor.execute(dequeueAll());
        }
    }

    public void dispose() {
        _running = false;

        execute(new Runnable() {
            public void run() {
                // so it wakes up and will notice that we've told it to stop
            }
        });

        synchronized (_disposables) {
            //copy list to prevent concurrent mod
            for (Disposable r : _disposables.toArray(new Disposable[_disposables.size()])) {
                r.dispose();
            }
        }
    }

    public void add(Disposable r) {
        _disposables.add(r);
    }

    public boolean remove(Disposable disposable) {
        return _disposables.remove(disposable);
    }

    public int size() {
        return _disposables.size();
    }
}

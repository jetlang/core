package org.jetlang.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation that queues and executes events. A dedicated thread is typically
 * used to consume events.
 */
public class RunnableExecutorImpl implements RunnableExecutor {

    private final RunnableBlockingQueue _commands = new RunnableBlockingQueue();
    private final List<Disposable> _disposables = Collections.synchronizedList(new ArrayList<Disposable>());

    private final BatchExecutor _commandExecutor;

    public RunnableExecutorImpl() {
        this(new BatchExecutorImpl());
    }

    public RunnableExecutorImpl(BatchExecutor executor) {
        _commandExecutor = executor;
    }

    public void execute(Runnable command) {
        _commands.put(command);
    }

    private Runnable[] dequeueAll() {
        return _commands.sweep();
    }

    public void run() {
        while (_commands.isRunning()) {
            _commandExecutor.execute(dequeueAll());
        }
    }

    public void dispose() {
        _commands.setRunning(false);

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

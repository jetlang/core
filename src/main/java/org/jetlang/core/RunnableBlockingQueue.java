package org.jetlang.core;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Blocking queue supporting efficient put and sweep operations.
 *
 * @author mrettig
 */
public class RunnableBlockingQueue {

    private volatile boolean _running = true;
    private final Lock _lock = new ReentrantLock();
    private final Condition _waiter = _lock.newCondition();
    private EventBuffer _queue = new EventBuffer();

    public boolean isRunning() {
        return _running;
    }

    public void setRunning(boolean isRunning) {
        this._running = isRunning;
    }

    public void put(Runnable r) {
        _lock.lock();
        try {
            _queue.add(r);
            _waiter.signal();
        } finally {
            _lock.unlock();
        }
    }

    public EventBuffer swap(EventBuffer buffer) {
        _lock.lock();
        try {
            while (_queue.isEmpty() && _running) {
                try {
                    _waiter.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            EventBuffer toReturn = _queue;
            _queue = buffer;
            return toReturn;
        } finally {
            _lock.unlock();
        }
    }

    public boolean isEmpty() {
        _lock.lock();
        try {
            return _queue.isEmpty();
        } finally {
            _lock.unlock();
        }
    }
}

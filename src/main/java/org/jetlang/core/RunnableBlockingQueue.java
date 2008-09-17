package org.jetlang.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

/**
 * Blocking queue supporting efficient put and sweep operations.
 *
 * @author mrettig
 */
public class RunnableBlockingQueue {

    private volatile boolean _running = true;
    private Lock _lock = new ReentrantLock();
    private Condition _waiter = _lock.newCondition();
    private final List<Runnable> _queue = new ArrayList<Runnable>();

    public boolean isRunning() {
        return _running;
    }

    public void setRunning(boolean isRunning) {
        this._running = isRunning;
    }

    public void put(Runnable r) {
        _lock.lock();
        try{
            _queue.add(r);
            _waiter.signal();
        }finally{
            _lock.unlock();
        }
    }

    public Runnable[] sweep() {
        _lock.lock();
        try {
            while (_queue.isEmpty() && _running) {
                try {
                    _waiter.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            Runnable[] result = _queue.toArray(new Runnable[_queue.size()]);
            _queue.clear();
            return result;
        }finally{
            _lock.unlock();
        }
    }

    public boolean isEmpty() {
        _lock.lock();
        try{
            return _queue.isEmpty();
        }finally{
            _lock.unlock();
        }
    }
}

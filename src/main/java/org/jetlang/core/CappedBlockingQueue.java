package org.jetlang.core;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Adds a cap to the size of the queue. Producers will block until space becomes available.
 * Can potentially deadlock a fiber if the consuming thread puts an event to its own full queue. To be safe use another thread
 * to post events back to the same fiber.
 *
 * @author mrettig
 */
public class CappedBlockingQueue implements EventQueue {

    private volatile boolean _running = true;
    private final Lock _lock = new ReentrantLock();
    private final Condition empty = _lock.newCondition();
    private final Condition full = _lock.newCondition();
    private EventBuffer _queue = new EventBuffer();
    private final int cap;

    public CappedBlockingQueue(int maxQueueSize) {
        this.cap = maxQueueSize;
    }

    public boolean isRunning() {
        return _running;
    }

    public void setRunning(boolean isRunning) {
        this._running = isRunning;
    }

    public void put(Runnable r) {
        _lock.lock();
        try {
            while (_queue.size() >= cap && _running) {
                try {
                    full.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (!_running)
                return;
            _queue.add(r);
            empty.signal();
        } finally {
            _lock.unlock();
        }
    }

    public EventBuffer swap(EventBuffer buffer) {

        _lock.lock();
        try {
            while (_queue.isEmpty() && _running) {
                try {
                    empty.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            EventBuffer toReturn = _queue;
            _queue = buffer;
            full.signalAll();
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

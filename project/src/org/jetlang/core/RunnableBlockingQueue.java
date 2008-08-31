package org.jetlang.core;

import java.util.ArrayList;

/**
 * Blocking queue supporting efficient put and sweep operations.
 *
 * @author mrettig
 */
public class RunnableBlockingQueue {

    private volatile boolean _running = true;
    private final ArrayList<Runnable> _queue = new ArrayList<Runnable>();

    public boolean isRunning() {
        return _running;
    }

    public void setRunning(boolean isRunning) {
        this._running = isRunning;
    }

    public void put(Runnable r) {
        synchronized (_queue) {
            _queue.add(r);
            _queue.notify();
        }
    }

    public Runnable[] sweep() {
        synchronized (_queue) {
            while (_queue.size() == 0 && _running) {
                try {
                    _queue.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            Runnable[] result = _queue.toArray(new Runnable[_queue.size()]);
            _queue.clear();
            return result;
        }
    }

    public boolean isEmpty() {
        synchronized (_queue) {
            return _queue.size() == 0;
        }
    }
}

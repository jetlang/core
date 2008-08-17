package org.jetlang.core;

import java.util.ArrayList;

/**
 * User: mrettig
 * Date: Aug 17, 2008
 * Time: 5:51:18 PM
 */
public class RunnableBlockingQueue {

    private volatile boolean _running = true;
    private final ArrayList<Runnable> _queue = new ArrayList<Runnable>();

    public boolean isRunning() {
        return _running;
    }

    public void setRunning(boolean _running) {
        this._running = _running;
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

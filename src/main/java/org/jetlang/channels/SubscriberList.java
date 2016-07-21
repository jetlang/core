package org.jetlang.channels;

import org.jetlang.core.Callback;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: mrettig
 * Date: Aug 29, 2009
 */
public class SubscriberList<T> {

    private static final Callback[] EMPTY = new Callback[0];

    private volatile Callback<T>[] subscribers = EMPTY;
    private final Lock lock = new ReentrantLock();

    public void add(Callback<T> cb) {
        lock.lock();
        try {
            Callback<T>[] resized = new Callback[subscribers.length + 1];
            System.arraycopy(subscribers, 0, resized, 0, subscribers.length);
            resized[subscribers.length] = cb;
            subscribers = resized;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        return subscribers.length;
    }

    public void clear() {
        lock.lock();
        try {
            subscribers = EMPTY;
        }
        finally {
            lock.unlock();
        }
    }

    public boolean remove(Callback<T> cb) {
        lock.lock();
        try {
            int index = -1;
            final Callback[] cbs = subscribers;
            for (int i = 0; i < cbs.length; i++) {
                if (cbs[i].equals(cb)) {
                    index = i;
                    break;
                }
            }
            if (index == -1)
                return false;
            Callback<T>[] resized = new Callback[subscribers.length - 1];
            System.arraycopy(subscribers, 0, resized, 0, index);
            if (index != resized.length)
                System.arraycopy(subscribers, index + 1, resized, index, resized.length - index);

            subscribers = resized;
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void publish(T msg) {
        executeAll(msg, subscribers);
    }

    private static <V> void executeAll(final V msg, final Callback<V>[] cbs) {
        for (Callback<V> cb : cbs) {
            cb.onMessage(msg);
        }
    }
}

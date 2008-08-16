package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.Disposable;
import org.jetlang.core.DisposingExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: mrettig
 * Date: Jul 22, 2008
 * Time: 3:57:21 PM
 */
public class MemoryChannel<T> implements Channel<T> {

    private final List<Callback<T>> _subscribers = Collections.synchronizedList(new ArrayList<Callback<T>>());

    public int subscriberCount() {
        return _subscribers.size();
    }

    public int publish(T s) {
        synchronized (_subscribers) {
            for (Callback<T> callback : _subscribers) {
                callback.onMessage(s);
            }
            return _subscribers.size();
        }
    }

    public Disposable subscribe(DisposingExecutor queue, Callback<T> onReceive) {
        ChannelSubscription<T> subber = new ChannelSubscription<T>(queue, onReceive);
        return subscribe(subber);
    }

    public Disposable subscribe(Subscribable<T> sub) {
        return subscribeOnProducerThread(sub.getQueue(), sub);
    }

    public Disposable subscribeOnProducerThread(final DisposingExecutor queue, final Callback<T> callbackOnQueue) {
        Disposable unSub = new Disposable() {
            public void dispose() {
                Remove(callbackOnQueue);
                queue.remove(this);
            }
        };
        queue.add(unSub);
        //finally add subscription to start receiving events.
        _subscribers.add(callbackOnQueue);
        return unSub;
    }

    private void Remove(Callback<T> callbackOnQueue) {
        _subscribers.remove(callbackOnQueue);
    }

    public void clearSubscribers() {
        _subscribers.clear();
    }
}

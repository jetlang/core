package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.Disposable;
import org.jetlang.core.DisposingExecutor;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * User: mrettig
 * Date: Jul 22, 2008
 * Time: 3:57:21 PM
 */
public class MemoryChannel<T> implements Channel<T> {

    private final CopyOnWriteArrayList<Callback<T>> _subscribers = new CopyOnWriteArrayList<Callback<T>>();

    public int subscriberCount() {
        return _subscribers.size();
    }

    public void publish(T s) {
        for (Callback<T> subscriber : _subscribers) {
            subscriber.onMessage(s);
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
                remove(callbackOnQueue);
                queue.remove(this);
            }
        };
        queue.add(unSub);
        //finally add subscription to start receiving events.
        _subscribers.add(callbackOnQueue);
        return unSub;
    }

    private void remove(Callback<T> callbackOnQueue) {
        _subscribers.remove(callbackOnQueue);
    }

    public void clearSubscribers() {
        _subscribers.clear();
    }
}

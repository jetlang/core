package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.Disposable;
import org.jetlang.core.DisposingExecutor;

/**
 * Conduit for exchanging messages between threads. Objects references will be delivered
 * between threads without any serialization or object copying.
 * <p/>
 * Provides method for publishing and subscribing to events.
 *
 * @author mrettig
 */
public class MemoryChannel<T> implements Channel<T> {

    private final SubscriberList<T> _subscribers = new SubscriberList<T>();

    public int subscriberCount() {
        return _subscribers.size();
    }

    public void publish(T s) {
        _subscribers.publish(s);
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

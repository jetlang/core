package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.RunnableQueue;
import org.jetlang.core.Stopable;

import java.util.ArrayList;

/**
 * User: mrettig
 * Date: Jul 22, 2008
 * Time: 3:57:21 PM
 */
public class Channel<T> implements ChannelPublisher<T>, ChannelSubscriber<T> {

    private final ArrayList<Callback<T>> _subscribers = new ArrayList<Callback<T>>();

    public int subscriberCount() {
        synchronized (_subscribers) {
            return _subscribers.size();
        }
    }

    public int publish(T s) {
        synchronized (_subscribers) {
            for (Callback<T> callback : _subscribers) {
                callback.onMessage(s);
            }
            return _subscribers.size();
        }
    }

    public Stopable subscribe(final RunnableQueue queue, final Callback<T> onReceive) {
        final Callback<T> callbackOnQueue = new Callback<T>() {
            public void onMessage(final T message) {
                final Runnable toExecute = new Runnable() {
                    public void run() {
                        onReceive.onMessage(message);
                    }
                };
                queue.execute(toExecute);
            }
        };
        return subscribeOnProducerThread(queue, callbackOnQueue);
    }

    public Stopable subscribe(final Subscribable<T> sub) {
        return subscribeOnProducerThread(sub.getQueue(), sub);
    }

    public Stopable subscribeOnProducerThread(RunnableQueue queue, final Callback<T> callbackOnQueue) {
        synchronized (_subscribers) {
            _subscribers.add(callbackOnQueue);
        }
        final Stopable unSub = new Stopable() {
            public void stop() {
                Remove(callbackOnQueue);
            }
        };
        queue.onStop(unSub);
        return unSub;
    }

    private void Remove(Callback<T> callbackOnQueue) {
        synchronized (_subscribers) {
            _subscribers.remove(callbackOnQueue);
        }

    }

    public void clearSubscribers() {
        synchronized (_subscribers) {
            _subscribers.clear();
        }
    }
}

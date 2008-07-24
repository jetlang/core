package com.jetlang.channels;

import com.jetlang.core.Callback;
import com.jetlang.core.ICommandQueue;
import com.jetlang.core.Unsubscriber;

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

    public boolean publish(T s) {
        boolean published = false;
        synchronized (_subscribers) {
            for (Callback<T> callback : _subscribers) {
                callback.onMessage(s);
                published = true;
            }
        }
        return published;
    }

    public Unsubscriber subscribe(final ICommandQueue queue, final Callback<T> onReceive) {
        final Callback<T> callbackOnQueue = new Callback<T>() {
            public void onMessage(final T message) {
                final Runnable toExecute = new Runnable() {
                    public void run() {
                        onReceive.onMessage(message);
                    }
                };
                queue.queue(toExecute);
            }
        };
        synchronized (_subscribers) {
            _subscribers.add(callbackOnQueue);
        }
        final Runnable unSub = new Runnable() {
            public void run() {
                Remove(callbackOnQueue);
            }
        };
        queue.onStop(unSub);
        return new Unsubscriber(unSub);
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

package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.Disposable;
import org.jetlang.fibers.Fiber;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * User: mrettig
 * Date: Jan 31, 2009
 * Time: 5:10:16 PM
 */
public class SingleReply {

    public static <R, V> Disposable publish(Fiber fiber,
                                            RequestChannel<R, V> channel, R request, final Callback<V> reply) {
        AsyncRequest<R, V> async = new AsyncRequest<R, V>(fiber);
        async.setResponseCount(1);

        Callback<List<V>> onMsg = new Callback<List<V>>() {
            public void onMessage(List<V> message) {
                reply.onMessage(message.get(0));
            }
        };
        return async.publish(channel, request, onMsg);
    }

    public static <R, V> Disposable publish(Fiber fiber,
                                            RequestChannel<R, V> channel, R request, final Callback<V> reply,
                                            long timeout, TimeUnit unit, final Runnable onTimeout) {
        AsyncRequest<R, V> async = new AsyncRequest<R, V>(fiber);
        async.setResponseCount(1);
        final Callback<List<V>> onListTimeout = new Callback<List<V>>() {
            public void onMessage(List<V> message) {
                onTimeout.run();
            }
        };
        async.setTimeout(onListTimeout, timeout, unit);
        Callback<List<V>> onMsg = new Callback<List<V>>() {
            public void onMessage(List<V> message) {
                reply.onMessage(message.get(0));
            }
        };
        return async.publish(channel, request, onMsg);
    }
}

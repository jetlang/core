package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.Disposable;
import org.jetlang.fibers.Fiber;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * User: mrettig
 * Date: Jan 31, 2009
 * Time: 1:31:46 PM
 */
public class AsyncRequest<R, V> {
    private Fiber target;
    private BatchTimeout<V> timeout;
    private int responses = 1;

    public AsyncRequest(Fiber target) {
        this.target = target;
    }

    public AsyncRequest<R, V> setTimeout(Callback<List<V>> onTimeout, long time, TimeUnit unit) {
        timeout = new BatchTimeout<V>(onTimeout, time, unit);
        return this;
    }

    public AsyncRequest<R, V> setResponseCount(int responses) {
        this.responses = responses;
        return this;
    }

    public Disposable publish(RequestChannel<R, V> channel, R req, Callback<List<V>> onResponse) {
        BatchCallback<R, V> callback = new BatchCallback<R, V>(responses, onResponse, timeout);
        callback.send(channel, req, target);
        return callback;
    }

    public static <R, V> Disposable withOneReply(Fiber fiber, RequestChannel<R, V> channel, R req, Callback<V> onReply) {
        return SingleReply.publish(fiber, channel, req, onReply);
    }

    public static <R, V> Disposable withOneReply(Fiber fiber, RequestChannel<R, V> channel, R req, Callback<V> onReply,
                                                 long timeout, TimeUnit unit, Runnable onTimeout) {
        return SingleReply.publish(fiber, channel, req, onReply, timeout, unit, onTimeout);
    }

}

class BatchTimeout<V> {
    Callback<List<V>> cb;
    long time;
    TimeUnit unit;

    public BatchTimeout(Callback<List<V>> cb, long time, TimeUnit unit) {
        this.cb = cb;
        this.time = time;
        this.unit = unit;
    }

}



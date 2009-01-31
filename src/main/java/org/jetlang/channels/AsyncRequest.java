package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.Disposable;
import org.jetlang.fibers.Fiber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * User: mrettig
 * Date: Jan 31, 2009
 * Time: 1:31:46 PM
 */
public class AsyncRequest<R, V> {
    private Fiber target;
    private BatchTimeout timeout;
    private int responses = 1;

    public AsyncRequest(Fiber target) {
        this.target = target;
    }

    public AsyncRequest<R, V> setTimeout(Callback<List<V>> onTimeout, long time, TimeUnit unit) {
        timeout = new BatchTimeout(onTimeout, time, unit);
        return this;
    }

    public AsyncRequest<R, V> setResponseCount(int responses) {
        this.responses = responses;
        return this;
    }

    public Disposable publish(RequestChannel<R, V> channel, R req, Callback<List<V>> onResponse) {
        Callback<List<V>> time = null;
        if (timeout != null) {
            time = timeout.cb;
        }
        BatchCallback callback = new BatchCallback(time, responses, onResponse);
        final Disposable d = channel.publish(target, req, callback);
        if (timeout != null) {
            final Disposable timer = target.schedule(callback, timeout.time, timeout.unit);
            Disposable dis = new Disposable() {
                public void dispose() {
                    d.dispose();
                    timer.dispose();
                }
            };
            callback.setDispose(dis);
        } else {
            callback.setDispose(d);
        }
        return callback;
    }

    private class BatchCallback implements Callback<V>, Runnable, Disposable {
        private final List<V> results = new ArrayList<V>();
        private final Callback<List<V>> onTimeout;
        private int responses;
        private Callback<List<V>> onComplete;
        private Disposable d;

        public BatchCallback(Callback<List<V>> onTimeout, int responses, Callback<List<V>> onComplete) {
            this.onTimeout = onTimeout;
            this.responses = responses;
            this.onComplete = onComplete;
        }

        public void onMessage(V message) {
            results.add(message);
            if (responses > 0 && results.size() == responses) {
                dispose();
                onComplete.onMessage(results);
            }
        }

        public void run() {
            dispose();
            onTimeout.onMessage(results);
        }

        public void setDispose(Disposable d) {
            this.d = d;
        }

        public void dispose() {
            if (d != null) {
                d.dispose();
            }
        }
    }

    class BatchTimeout {
        private Callback<List<V>> cb;
        private long time;
        private TimeUnit unit;

        public BatchTimeout(Callback<List<V>> cb, long time, TimeUnit unit) {
            this.cb = cb;
            this.time = time;
            this.unit = unit;
        }

    }

}


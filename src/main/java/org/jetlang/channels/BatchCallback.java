package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.Disposable;
import org.jetlang.fibers.Fiber;

import java.util.ArrayList;
import java.util.List;

class BatchCallback<R, V> implements Callback<V>, Runnable, Disposable {
    private final Object lock = new Object();
    private final List<V> results = new ArrayList<>();
    private final int responses;
    private final Callback<List<V>> onComplete;
    private final BatchTimeout<V> timeout;
    private Disposable d;

    public BatchCallback(int responses, Callback<List<V>> onComplete, BatchTimeout<V> timeout) {
        this.responses = responses;
        this.onComplete = onComplete;
        this.timeout = timeout;
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
        timeout.cb.onMessage(results);
    }

    public void dispose() {
        synchronized (lock) {
            if (d != null) {
                d.dispose();
            }
        }
    }

    public void send(RequestChannel<R, V> channel, R req, Fiber target) {
        // send may occur from a different thread than the receive thread.
        // Once the message is sent there is a race. The lock prevents the receiving thread from
        // attempting to dispose before the variables are properly set.
        synchronized (lock) {
            final Disposable requestDispose = channel.publish(target, req, this);
            if (timeout != null) {
                final Disposable timer = target.schedule(this, timeout.time, timeout.unit);
                this.d = new Disposable() {
                    public void dispose() {
                        requestDispose.dispose();
                        timer.dispose();
                    }
                };
            } else {
                d = requestDispose;
            }
        }
    }
}

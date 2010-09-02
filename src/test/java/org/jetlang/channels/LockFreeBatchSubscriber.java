package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.Filter;
import org.jetlang.fibers.Fiber;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ruslan
 *         created 22.08.2010 at 12:20:32
 */
public class LockFreeBatchSubscriber<T> extends BaseSubscription<T> {

    private final Fiber executor;

    private final Callback<ConcurrentLinkedQueue<T>> callback;

    private final int interval;
    private final TimeUnit unit;

    /**
     * number of currently pending messages
     */
    private final AtomicBoolean pending = new AtomicBoolean(false);

    /**
     * we need lock-free Queue implementation here
     */
    private final ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<T>();
    private final Runnable flushRunnable;


    public LockFreeBatchSubscriber(final Fiber executor,
                                   final Callback<ConcurrentLinkedQueue<T>> receive,
                                   final Filter<T> filter,
                                   final int interval,
                                   final TimeUnit timeUnit) {
        super(executor, filter);

        this.executor = executor;
        this.callback = receive;
        this.interval = interval;
        this.unit = timeUnit;
        this.flushRunnable = new Runnable() {
            public void run() {
                flush();
            }

            @Override
            public String toString() {
                return "Flushing" + LockFreeBatchSubscriber.this + " via " + callback.toString();
            }
        };
    }

    public LockFreeBatchSubscriber(final Fiber queue,
                                   final Callback<ConcurrentLinkedQueue<T>> receive,
                                   final int interval,
                                   final TimeUnit timeUnit) {
        this(queue, receive, null, interval, timeUnit);
    }

    protected void onMessageOnProducerThread(final T msg) {
        queue.add(msg);

        if (pending.compareAndSet(false, true)) {
            schedule();
        }
    }

    private void flush() {
        try {
            callback.onMessage(queue);
        } finally {
            pending.compareAndSet(true, false);
            if (!queue.isEmpty()) {
                if (pending.compareAndSet(false, true)) {
                    schedule();
                }
            }
        }
    }

    private void schedule() {
        executor.schedule(flushRunnable, interval, unit);
    }

}

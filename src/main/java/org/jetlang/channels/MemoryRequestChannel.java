package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.Disposable;
import org.jetlang.core.DisposingExecutor;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: mrettig
 * Date: Jan 31, 2009
 * Time: 10:42:42 AM
 */
public class MemoryRequestChannel<R, V> implements RequestChannel<R, V> {

    private final MemoryChannel<Request<R, V>> channel = new MemoryChannel<>();
    private final MemoryChannel<SessionClosed<R>> endChannel = new MemoryChannel<>();

    public Disposable subscribe(DisposingExecutor fiber, Callback<Request<R, V>> onRequest) {
        return channel.subscribe(fiber, onRequest);
    }

    public Disposable subscribe(DisposingExecutor fiber, Callback<Request<R, V>> onRequest, Callback<SessionClosed<R>> onRequestEnd) {
        final Disposable sub = channel.subscribe(fiber, onRequest);
        final Disposable end = endChannel.subscribe(fiber, onRequestEnd);
        return new Disposable() {
            public void dispose() {
                sub.dispose();
                end.dispose();
            }
        };
    }

    public Disposable publish(DisposingExecutor target, final R request, Callback<V> reply) {
        final RequestImpl req = new RequestImpl(target, request, reply);
        channel.publish(req);
        return new Disposable() {
            public void dispose() {
                if (req.dispose()) {
                    SessionClosed<R> end = new SessionClosedImpl<>(request, req.getSession());
                    endChannel.publish(end);
                }
            }
        };
    }

    private static class SessionImpl implements Session {

    }

    private class RequestImpl implements Request<R, V> {
        private final DisposingExecutor target;
        private final R request;
        private final Callback<V> reply;
        private final AtomicBoolean disposed = new AtomicBoolean(false);
        private final Session session = new SessionImpl();

        public RequestImpl(DisposingExecutor target, R request, Callback<V> reply) {
            this.target = target;
            this.request = request;
            this.reply = reply;
        }

        public Session getSession() {
            return session;
        }

        public R getRequest() {
            return request;
        }

        public void reply(final V msg) {
            Runnable onMsg = new Runnable() {
                public void run() {
                    consumeMsg(msg);
                }
            };
            target.execute(onMsg);
        }

        private void consumeMsg(V msg) {
            if (disposed.get()) {
                return;
            }
            reply.onMessage(msg);
        }

        public boolean dispose() {
            return disposed.compareAndSet(false, true);
        }

    }
}

class SessionClosedImpl<R> implements SessionClosed<R> {
    private final R request;
    private final Session s;

    public SessionClosedImpl(R request, Session s) {
        this.request = request;
        this.s = s;
    }

    public R getOriginalRequest() {
        return request;
    }

    public Session getSession() {
        return s;
    }
}

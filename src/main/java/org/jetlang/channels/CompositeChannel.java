package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.Disposable;
import org.jetlang.core.DisposingExecutor;

/**
 * User: mrettig
 * Date: Sep 6, 2009
 */
public class CompositeChannel<T> implements Channel<T> {

    private final Channel<T>[] channels;

    public CompositeChannel(Channel<T>... channels) {
        this.channels = channels;
    }


    public Disposable subscribe(DisposingExecutor executor, Callback<T> receive) {
        final Disposable[] all = new Disposable[channels.length];
        Disposable d = new Disposable() {

            public void dispose() {
                for (Disposable disposable : all) {
                    disposable.dispose();
                }
            }
        };

        for (int i = 0; i < channels.length; i++) {
            all[i] = channels[i].subscribe(executor, receive);
        }

        return d;
    }

    public Disposable subscribe(Subscribable<T> sub) {
        final Disposable[] all = new Disposable[channels.length];
        Disposable d = new Disposable() {

            public void dispose() {
                for (Disposable disposable : all) {
                    disposable.dispose();
                }
            }
        };

        for (int i = 0; i < channels.length; i++) {
            all[i] = channels[i].subscribe(sub);
        }

        return d;
    }

    public void publish(T msg) {
        for (int i = 0; i < channels.length; i++) {
            channels[i].publish(msg);
        }
    }
}

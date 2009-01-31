package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.Disposable;
import org.jetlang.core.DisposingExecutor;

/**
 * User: mrettig
 * Date: Jan 31, 2009
 * Time: 10:23:38 AM
 */
public interface RequestChannel<R, V> {

    Disposable subscribe(DisposingExecutor fiber, Callback<Request<R, V>> onRequest);

    public Disposable publish(DisposingExecutor fiber, R request, Callback<V> reply);

}

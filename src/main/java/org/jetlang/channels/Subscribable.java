package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.DisposingExecutor;

/**
 * Interface to subscribe to events on producing thread(s). Implementations
 * should be thread safe.
 */
public interface Subscribable<T> extends Callback<T> {

    DisposingExecutor getQueue();

}

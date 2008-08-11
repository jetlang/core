package org.jetlang.core;

import java.util.concurrent.Executor;

/**
 * An {@link Executor} that can dispose other components in unison with itself.
 */
public interface DisposingExecutor extends Executor {

    /**
     * Add a {@link Disposable} to be disposed when this component is disposed
     *
     * @param disposable Disposable instance. Should not be null.
     */
    void add(Disposable disposable);

    /**
     * Remove a {@link Disposable} from being disposed when this component is disposed
     *
     * @param disposable Disposable instance. Should not be null
     * @return True if the supplied Disposable was successfully removed. False if it wasn't available to be removed
     */
    boolean remove(Disposable disposable);

    /**
     * Return the number of {@link Disposable} instances registered with this instance.
     * <p/>
     * This method is typically used for testing and debugging purposes.
     *
     * @return Number of {@link Disposable} instances registered.
     */
    int size();
}

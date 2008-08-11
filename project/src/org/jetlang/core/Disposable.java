package org.jetlang.core;

/**
 * Interface to represent an instance that requires explicit resource cleanup
 */
public interface Disposable {

    /**
     * Dispose this instance. It should be considered unusable after calling this method
     */
    void dispose();
}

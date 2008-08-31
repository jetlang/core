package org.jetlang.core;

/**
 * Event callback.
 */
public interface Callback<T> {
    void onMessage(T message);
}

package org.jetlang.core;

/**
 * Interface to represent a boolean filter for messages
 *
 * @param <T> Type of message this filter is for
 */
public interface Filter<T> {
    /**
     * Check to see whether the supplied message passes the filter
     *
     * @param msg Message to check against filter
     * @return True upon success, false otherwise.
     */
    boolean passes(T msg);
}

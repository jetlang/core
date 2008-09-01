package org.jetlang.channels;

/**
 * Interface for components that allow messages to be published to them
 */
public interface Publisher<T> {
    /**
     * Publish a message.
     *
     * @param msg Message to publish
     */
    void publish(T msg);
}

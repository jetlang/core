package org.jetlang.channels;

/**
 * Interface for components that allow messages to be published to them
 */
public interface Publisher<T> {
    /**
     * Publish a message.
     *
     * @param msg Message to publish
     * @return Number of subscribers that will receive this message at time of publication
     */
    int publish(T msg);
}

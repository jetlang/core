package org.jetlang.channels;

/**
 * Combined suscriber and publisher interface.
 */
public interface Channel<T> extends Subscriber<T>, Publisher<T> {
}

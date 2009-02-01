package org.jetlang.channels;

/**
 * Message sent when a request session is closed.
 */
public interface SessionClosed<R> {

    R getOriginalRequest();

    Session getSession();
}

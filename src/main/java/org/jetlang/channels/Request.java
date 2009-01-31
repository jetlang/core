package org.jetlang.channels;

/**
 * User: mrettig
 * Date: Jan 31, 2009
 * Time: 10:25:53 AM
 */
public interface Request<R, V> {
    R getRequest();

    void reply(V i);
}

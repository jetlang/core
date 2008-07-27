package org.jetlang.core;

/**
 * User: mrettig
 * Date: Jul 27, 2008
 * Time: 2:32:31 PM
 */
public interface Filter<T> {
    boolean passes(T msg);
}

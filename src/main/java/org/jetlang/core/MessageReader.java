package org.jetlang.core;

/**
 * User: mrettig
 * Date: Sep 28, 2009
 */
public interface MessageReader<T> {

    int size();

    T get(int index);

}

package org.jetlang.core;

/**
 * User: mrettig
 * Date: Aug 30, 2009
 */
public interface EventReader {
    int size();

    Runnable get(int index);
}

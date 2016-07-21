package org.jetlang.core;

import java.util.ArrayList;

/**
 * User: mrettig
 * Date: Sep 28, 2009
 */
public class MessageBuffer<T> implements MessageReader<T> {
    private ArrayList<T> events = new ArrayList<>();

    public int size() {
        return events.size();
    }

    public T get(int index) {
        return events.get(index);
    }

    public void add(T r) {
        events.add(r);
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    public void clear() {
        events.clear();
    }

}

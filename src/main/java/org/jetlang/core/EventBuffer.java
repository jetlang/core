package org.jetlang.core;

import java.util.ArrayList;

/**
 * User: mrettig
 * Date: Aug 29, 2009
 */
public class EventBuffer {

    private ArrayList<Runnable> events = new ArrayList<Runnable>();

    public int size() {
        return events.size();
    }

    public Runnable get(int index) {
        return events.get(index);
    }

    public void add(Runnable r) {
        events.add(r);
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    public void clear() {
        events.clear();
    }
}

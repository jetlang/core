package org.jetlang.core;

public interface EventQueue {
    boolean isRunning();

    void setRunning(boolean isRunning);

    void put(Runnable r);

    EventBuffer swap(EventBuffer buffer);

    boolean isEmpty();
}

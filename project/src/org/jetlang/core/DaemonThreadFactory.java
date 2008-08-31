package org.jetlang.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Creates daemon threads.
 *
 * @author mrettig
 */
class DaemonThreadFactory implements ThreadFactory {
    private ThreadFactory factory = Executors.defaultThreadFactory();

    public Thread newThread(Runnable r) {
        Thread t = factory.newThread(r);
        t.setDaemon(true);
        return t;
    }
}

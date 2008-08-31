package org.jetlang.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * User: mrettig
 * Date: Aug 30, 2008
 * Time: 8:56:32 PM
 */
class DaemonThreadFactory implements ThreadFactory {
    private ThreadFactory factory = Executors.defaultThreadFactory();

    public Thread newThread(Runnable r) {
        Thread t = factory.newThread(r);
        t.setDaemon(true);
        return t;
    }
}

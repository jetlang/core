package org.jetlang;

import org.jetlang.core.Disposable;

public class PerfTimer implements Disposable {
    private final int _count;
    private long _stopWatch;

    public PerfTimer(int count) {
        _count = count;
        _stopWatch = System.currentTimeMillis();
    }

    public void dispose() {
        long elapsed = System.currentTimeMillis() - _stopWatch;
        System.out.println("Elapsed: " + elapsed + " Events: " + _count);
        System.out.println("Avg/S: " + (_count / (elapsed / 1000.00)));
    }
}

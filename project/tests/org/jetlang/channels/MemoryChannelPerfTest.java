package org.jetlang.channels;

import org.jetlang.core.Callback;
import org.jetlang.core.SynchronousDisposingExecutor;

/**
 * User: mrettig
 * Date: Aug 31, 2008
 * Time: 8:44:54 AM
 */
public class MemoryChannelPerfTest {

    public static void main(String[] args) {
        MemoryChannel<String> channel = new MemoryChannel<String>();
        Callback<String> cb = new Callback<String>() {
            public void onMessage(String message) {
            }
        };
        channel.subscribe(new SynchronousDisposingExecutor(), cb);
        for (int i = 0; i < 1000000; i++) {
            channel.publish("hello");
        }
        int max = 500000000;
        long start = System.nanoTime();
        long startMsg = System.currentTimeMillis();
        for (int i = 0; i < max; i++) {
            channel.publish("hello");
        }
        long diff = System.nanoTime() - start;
        System.out.println("Time: " + (diff / 1000000.00));
        System.out.println("Ms: " + (System.currentTimeMillis() - startMsg));
    }
}

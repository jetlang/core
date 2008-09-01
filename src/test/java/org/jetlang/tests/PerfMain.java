package org.jetlang.tests;

import org.jetlang.channels.ChannelTest;

/**
 * User: mrettig
 * Date: Aug 11, 2008
 * Time: 4:07:51 PM
 */
public class PerfMain {

    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(30000);
        ChannelTest tests = new ChannelTest();
        tests.pointToPointPerfTestWithPool();
        tests.pointToPointPerfTestWithThread();
    }
}

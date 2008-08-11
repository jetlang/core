package org.jetlang.tests;

import org.jetlang.channels.ChannelTests;

/**
 * User: mrettig
 * Date: Aug 11, 2008
 * Time: 4:07:51 PM
 */
public class PerfMain {

    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(30000);
        ChannelTests tests = new ChannelTests();
        tests.pointToPointPerfTest();
    }
}

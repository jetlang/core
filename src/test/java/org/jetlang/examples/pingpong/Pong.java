package org.jetlang.examples.pingpong;

import org.jetlang.core.Callback;
import org.jetlang.fibers.Fiber;

/**
 * User: mrettig
 * Date: Dec 7, 2008
 * Time: 12:36:33 PM
 */
public class Pong {

    private PingPongChannels channels;
    private Fiber consumer;

    public Pong(PingPongChannels channels, Fiber fiber) {
        this.channels = channels;
        this.consumer = fiber;
    }

    public void start() {
        Callback<Integer> onReceive = new Callback<Integer>() {
            public void onMessage(Integer message) {
                channels.Pong.publish(message);
                if (message % 1000 == 0)
                    System.out.println("message = " + message);
            }
        };
        channels.Ping.subscribe(consumer, onReceive);

        Callback<Void> onStop = new Callback<Void>() {
            public void onMessage(Void message) {
                consumer.dispose();
            }
        };
        channels.Stop.subscribe(consumer, onStop);
        consumer.start();
    }
}
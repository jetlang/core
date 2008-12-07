package org.jetlang.examples.pingpong;

import org.jetlang.channels.BatchSubscriber;
import org.jetlang.core.Callback;
import org.jetlang.fibers.Fiber;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * User: mrettig
 * Date: Dec 7, 2008
 * Time: 12:36:33 PM
 */
public class Ping {

    private PingPongChannels channels;
    private Fiber consumer;
    private int total;

    public Ping(PingPongChannels channels, Fiber fiber, int total) {
        this.channels = channels;
        this.consumer = fiber;
        this.total = total;
    }

    public void start() {
        Callback<Integer> onReceive = new Callback<Integer>() {
            public void onMessage(Integer message) {
                if (total > 0) {
                    publishPing();
                } else {
                    channels.Stop.publish(null);
                    consumer.dispose();
                }
            }
        };
        channels.Pong.subscribe(consumer, onReceive);
        consumer.start();

        //send first ping from ping fiber. The first ping could have been published from the main
        // thread as well, but in this case we'll use the ping fiber to be consistent.
        Runnable firstPing = new Runnable() {
            public void run() {
                publishPing();
            }
        };
        consumer.execute(firstPing);

        Callback<List<Integer>> onBatch = new Callback<List<Integer>>() {

            public void onMessage(List<Integer> message) {
                //consume all messages.
            }
        };
        BatchSubscriber<Integer> sub = new BatchSubscriber<Integer>(consumer, onBatch, 0, TimeUnit.MILLISECONDS);
        channels.Ping.subscribe(consumer, sub);

    }

    private void publishPing() {
        total--;
        channels.Ping.publish(total);
    }
}

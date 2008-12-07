package org.jetlang.examples.pingpong;

import org.jetlang.channels.Channel;
import org.jetlang.fibers.Fiber;
import org.jetlang.core.Callback;

/**
 * User: mrettig
 * Date: Dec 7, 2008
 * Time: 12:36:33 PM
 */
public class Ping {

    private PingPongChannels channels;
    private Fiber consumingThread;
    private int total;

    public Ping(PingPongChannels channels, Fiber consumingThread, int total){
        this.channels = channels;
        this.consumingThread = consumingThread;
        this.total = total;
    }

    public void start(){
        Callback<Integer> onReceive = new Callback<Integer>(){
            public void onMessage(Integer message) {
                if(total > 0){
                    publishPing();
                }
                else{
                    channels.Stop.publish(null);
                    consumingThread.dispose();
                }
            }
        };
        channels.Pong.subscribe(consumingThread, onReceive);
        consumingThread.start();
        Runnable firstPing = new Runnable(){
            public void run() {
                publishPing();
            }
        };
        consumingThread.execute(firstPing);
    }

    private void publishPing() {
        total--;
        channels.Ping.publish(total);
    }
}

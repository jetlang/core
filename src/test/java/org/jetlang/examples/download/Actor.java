package org.jetlang.examples.download;

import org.jetlang.channels.Channel;
import org.jetlang.core.Callback;
import org.jetlang.fibers.Fiber;

public abstract class Actor {

    private Channel<String> inChannel;
    private Channel<String> outChannel;
    private Channel<Void> stopChannel;
    private Channel<Void> nextStopChannel;
    private Fiber fiber;

    public Actor(Channel<String> inChannel,
                 Channel<String> outChannel,
                 Channel<Void> stopChannel,
                 Channel<Void> nextStopChannel,
                 Fiber fiber) {
        this.inChannel = inChannel;
        this.outChannel = outChannel;
        this.stopChannel = stopChannel;
        this.nextStopChannel = nextStopChannel;
        this.fiber = fiber;
    }

    public void start() {
        // set up subscription listener
        Callback<String> onRecieve = new Callback<String>() {
            public void onMessage(String message) {
                String newMsg = act(message);
                Main.Log(newMsg);
                if (outChannel != null) {
                    outChannel.publish(newMsg);
                }
            }
        };
        // subscribe to incoming channel
        inChannel.subscribe(fiber, onRecieve);

        Callback<Void> onStop = new Callback<Void>() {
            public void onMessage(Void message) {
                if (nextStopChannel != null) {
                    nextStopChannel.publish(null);
                }
                fiber.dispose();
            }
        };
        stopChannel.subscribe(fiber, onStop);
        // start the fiber
        fiber.start();
    }

    public abstract String act(String message);
}
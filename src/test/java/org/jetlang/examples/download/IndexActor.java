package org.jetlang.examples.download;


import org.jetlang.channels.Channel;
import org.jetlang.fibers.Fiber;

public class IndexActor extends Actor {

    public IndexActor(Channel<String> inChannel,
                      Channel<String> outChannel,
                      Channel<Void> stopChannel,
                      Channel<Void> nextStopChannel,
                      Fiber fiber) {
        super(inChannel, outChannel, stopChannel, nextStopChannel, fiber);
    }

    @Override
    public String act(String payload) {
        return payload.replaceFirst("Downloaded ", "Indexed ");
    }
}
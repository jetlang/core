package org.jetlang.core;

import org.jetlang.channels.Channel;
import org.jetlang.channels.MemoryChannel;
import org.junit.Test;

import static org.junit.Assert.fail;

public class SynchronousDisposingExecutorTest {

    @Test
    public void dispose(){
        SynchronousDisposingExecutor exec = new SynchronousDisposingExecutor();
        Channel<String> channel = new MemoryChannel<String>();
        channel.subscribe(exec, new Callback<String>() {
            public void onMessage(String message) {
                fail(message);
            }
        });
        exec.dispose();
        channel.publish("shouldn't be received");
    }
}

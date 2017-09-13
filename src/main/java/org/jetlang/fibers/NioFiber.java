package org.jetlang.fibers;

import org.jetlang.core.Callback;

import java.nio.channels.SelectableChannel;

public interface NioFiber extends Fiber {

    /**
     * if called from the selector thread, it will immediately
     * unregister and close the channel, otherwise it asynchronously
     * removes the channel.
     */
    void close(final SelectableChannel channel);

    /*
    Checks to see if the currently executing thread is the selector thread.
    Often used to determine whether the selector state can be immediately modified
    or changed asynchronously.
     */
    boolean onSelectorThread();

    void addHandler(NioChannelHandler handler);
    void removehandler(NioChannelHandler handler);

    /**
     * Asynchronously executes an event on the selector thread. The controls
     * allow the selector state to be modified.
     */
    void execute(Callback<NioControls> asyncWrite);
}

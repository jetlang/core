package org.jetlang.fibers;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public interface NioChannelHandler {

    enum Result {
        Continue, CloseSocket, RemoveHandler,
    }

    Result onSelect(NioFiber nioFiber, NioControls controls, SelectionKey key);

    SelectableChannel getChannel();

    int getInterestSet();

    void onEnd();

    void onSelectorEnd();

}

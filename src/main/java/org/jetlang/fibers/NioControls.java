package org.jetlang.fibers;

import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

public interface NioControls {
    void addHandler(NioChannelHandler handler);

    void write(SocketChannel accept, ByteBuffer buffer);

    boolean close(SelectableChannel channel);
}

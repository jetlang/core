package org.jetlang.fibers;

public interface NioFiber extends Fiber {
    void addHandler(NioChannelHandler handler);
}

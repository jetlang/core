package org.jetlang.fibers;

import org.jetlang.core.Callback;

public interface NioFiber extends Fiber {
    void addHandler(NioChannelHandler handler);

    void execute(Callback<NioControls> asyncWrite);
}

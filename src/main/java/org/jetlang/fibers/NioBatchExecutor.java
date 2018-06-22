package org.jetlang.fibers;

import org.jetlang.core.BatchExecutor;

import java.nio.channels.SelectionKey;

public interface NioBatchExecutor extends BatchExecutor {

    NioChannelHandler.Result runOnSelect(NioFiber fiber, NioChannelHandler handler, NioControls controls, SelectionKey key);
}

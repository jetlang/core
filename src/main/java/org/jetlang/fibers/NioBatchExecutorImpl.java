package org.jetlang.fibers;

import org.jetlang.core.BatchExecutorImpl;

import java.nio.channels.SelectionKey;

public class NioBatchExecutorImpl extends BatchExecutorImpl implements NioBatchExecutor {

    public NioChannelHandler.Result runOnSelect(NioFiber fiber, NioChannelHandler handler, NioControls controls, SelectionKey key) {
        return handler.onSelect(fiber, controls, key);
    }

}

package org.jetlang.fibers;

import org.jetlang.core.BatchExecutor;
import org.jetlang.core.BatchExecutorImpl;
import org.jetlang.core.EventReader;

import java.nio.channels.SelectionKey;

public interface NioBatchExecutor extends BatchExecutor {

    boolean runOnSelect(NioFiber fiber, NioChannelHandler handler, NioControls controls, SelectionKey key);
}

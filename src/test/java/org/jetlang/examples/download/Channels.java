package org.jetlang.examples.download;

import org.jetlang.channels.Channel;
import org.jetlang.channels.MemoryChannel;

public class Channels {

    public final Channel<String> downloadChannel =
            new MemoryChannel<String>();

    public final Channel<Void> downloadStopChannel =
            new MemoryChannel<Void>();


    public final Channel<String> indexChannel =
            new MemoryChannel<String>();

    public final Channel<Void> indexStopChannel =
            new MemoryChannel<Void>();


    public final Channel<String> writeChannel =
            new MemoryChannel<String>();

    public final Channel<Void> writeStopChannel =
            new MemoryChannel<Void>();

}
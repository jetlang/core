package org.jetlang.fibers;

import java.io.IOException;
import java.nio.channels.Pipe;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public interface NioChannelHandler {

    /**
     * @return true to continue
     */
    boolean onSelect(SelectionKey key);

    SelectableChannel getChannel();

    int getInterestSet();

    void onEnd();

    abstract class PipeReader implements NioChannelHandler {
        protected final Pipe.SinkChannel sink;
        protected final Pipe.SourceChannel source;

        public PipeReader() {
            try {
                final java.nio.channels.Pipe open = Pipe.open();
                sink = open.sink();
                sink.configureBlocking(false);
                source = open.source();
                source.configureBlocking(false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean onSelect(SelectionKey key) {
            return onData(source);
        }

        protected abstract boolean onData(Pipe.SourceChannel source);

        public SelectableChannel getChannel() {
            return source;
        }

        public int getInterestSet() {
            return SelectionKey.OP_READ;
        }

        public void onEnd() {
            try {
                source.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                sink.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public Pipe.SinkChannel getSink() {
            return sink;
        }
    }
}

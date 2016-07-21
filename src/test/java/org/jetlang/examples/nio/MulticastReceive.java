package org.jetlang.examples.nio;

import org.jetlang.fibers.NioChannelHandler;
import org.jetlang.fibers.NioControls;
import org.jetlang.fibers.NioFiber;
import org.jetlang.fibers.NioFiberImpl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public class MulticastReceive {

    public static void main(String[] args) throws IOException, InterruptedException {
        final NioFiberImpl nioFiber = new NioFiberImpl();
        final DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET);
        channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        channel.setOption(StandardSocketOptions.SO_RCVBUF, 1024 * 1024 * 2);
        channel.configureBlocking(false);
        nioFiber.addHandler(new NioChannelHandler() {
            final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(8);
            int count = 0;
            long duration = 0;
            int maxLoops = 0;
            long maxDur = 0;

            @Override
            public boolean onSelect(NioFiber nioFiber, NioControls controls, SelectionKey key) {
                int maxPackets = 10000000;
                int loops = 0;
                while (maxPackets-- > 0) {
                    try {
                        byteBuffer.clear();
                        if (channel.receive(byteBuffer) == null) {
                            return true;
                        }
                        loops++;
                        byteBuffer.flip();
                        long timestamp = byteBuffer.getLong();
                        final long currentDur = System.nanoTime() - timestamp;
                        duration += currentDur;
                        count++;
                        maxLoops = Math.max(loops, maxLoops);
                        maxDur = Math.max(currentDur, maxDur);
                        if (count == 100000) {
                            System.out.println(duration / count + " " + count + " " + duration + " " + maxLoops + " " + maxDur);
                            duration = 0;
                            count = 0;
                            maxLoops = 0;
                            maxDur = 0;
                        }
                    } catch (IOException e) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public SelectableChannel getChannel() {
                return channel;
            }

            @Override
            public int getInterestSet() {
                return SelectionKey.OP_READ;
            }

            @Override
            public void onEnd() {
            }

            @Override
            public void onSelectorEnd() {
            }
        });
        final String group = "239.8.128.3";
        final int port = 9999;
        final NetworkInterface nic = NetworkInterface.getByName("lo");
        nioFiber.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    channel.bind(new InetSocketAddress(group, port));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    InetAddress inetAddress = InetAddress.getByName(group);
                    channel.join(inetAddress, nic);
                } catch (IOException failed) {
                    throw new RuntimeException(failed);
                }
                System.out.println("Joined: " + group + ":" + port + " on " + nic);
            }
        });
        nioFiber.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Disposing");
                nioFiber.dispose();
                System.out.println("Done Disposing");
            }
        });
        Thread.sleep(Long.MAX_VALUE);
    }
}

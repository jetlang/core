package org.jetlang.examples.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class MulticastSend {

    public static void main(String[] args) throws IOException, InterruptedException {
        final DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET);
        channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        channel.setOption(StandardSocketOptions.SO_SNDBUF, 1024 * 1024 * 2);
        channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, NetworkInterface.getByName("lo"));
        channel.configureBlocking(true);
        final String group = "239.8.128.3";
        final int port = 9999;
        final NetworkInterface nic = NetworkInterface.getByName("eth1");
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(8);
        InetSocketAddress addr = new InetSocketAddress(group, port);
        for (int i = 0; i < 2000000; i++) {
            byteBuffer.putLong(System.nanoTime());
            byteBuffer.flip();
            channel.send(byteBuffer, addr);
            byteBuffer.clear();
            if (i % 100 == 0) {
                Thread.sleep(1);
            }
        }
        channel.close();
        System.out.println("done");
    }
}

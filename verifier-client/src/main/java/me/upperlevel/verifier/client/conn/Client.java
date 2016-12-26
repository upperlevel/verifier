package me.upperlevel.verifier.client.conn;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.logging.InternalLoggerFactory;
import me.upperlevel.verifier.client.conn.proto.MessagePacket;
import me.upperlevel.verifier.packetlib.PacketManager;

import java.util.Scanner;

public class Client {
    private final int port;
    private final String host;

    private final PacketManager manager = new PacketManager(PacketManager.SideType.CLIENT);

    public Client(int port, String host) {
        this.port = port;
        this.host = host;
        Thread.currentThread().setName("Verifier - Client");
    }

    public Client(String host) {
        this(25566, host);
    }

    public void start() throws InterruptedException {
        System.out.println("Using: " + InternalLoggerFactory.getDefaultFactory().getClass().getSimpleName());
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientInitializer(manager));

            registerPackets();

            Channel channel = bootstrap.connect(host, port).sync().channel();

            onConnectionStart(channel);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    private void registerPackets() {
        manager.register(MessagePacket.HANDLER);
    }

    protected void onConnectionStart(Channel channel) {
        Scanner in = new Scanner(System.in);
        while(true) {
            String line = in.nextLine();
            channel.writeAndFlush(new MessagePacket(line));
            System.out.println("Sent");
            if("bye".equals(line))
                break;
        }
    }
}

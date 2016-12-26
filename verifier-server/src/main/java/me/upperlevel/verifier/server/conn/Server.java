package me.upperlevel.verifier.server.conn;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.logging.InternalLoggerFactory;
import me.upperlevel.verifier.packetlib.PacketManager;
import me.upperlevel.verifier.server.conn.proto.MessagePacket;

public class Server {
    private final int port;
    private final PacketManager manager = new PacketManager(PacketManager.SideType.SERVER);

    public Server(int port) {
        this.port = port;
        Thread.currentThread().setName("Verifier - Server");
    }

    public Server() {
        this(25566);
    }

    public void start() throws InterruptedException {
        System.out.println("Using: " + InternalLoggerFactory.getDefaultFactory().getClass().getSimpleName());
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerInitializer(manager));

            registerPackets();

            // Start the server.
            ChannelFuture f = b.bind(port).sync().channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void registerPackets() {
        manager.register(MessagePacket.HANDLER);
    }
}

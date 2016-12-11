package me.upperlevel.verifier.conn;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.Getter;
import me.upperlevel.verifier.packetlib.Connection;
import me.upperlevel.verifier.packetlib.PacketManager;
import me.upperlevel.verifier.packetlib.ServerConnectionInitializer;

public class Client {
    private final int port;
    private final String host;
    @Getter
    private PacketManager<ServerHandler> packetManager = new PacketManager<>(ServerHandler.class);

    public Client(int port, String host) {
        this.port = port;
        this.host = host;
        Thread.currentThread().setName("Verifier - Server");
        packetManager.addListener(new ClientListener());
    }

    public Client(String host) {
        this(25566, host);
    }

    public void start() throws InterruptedException {
        System.out.println("Using: " + InternalLoggerFactory.getDefaultFactory().getClass().getSimpleName());
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap().group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ServerConnectionInitializer<>(this::onConnect, packetManager));

            Channel channel = bootstrap.connect(host, port).sync().channel();

            channel.write("Hi\n");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public ServerHandler onConnect(Connection connection) {
        ServerHandler handler = new ServerHandler(connection);
        //pools.addPool(handler);
        return handler;
    }
}

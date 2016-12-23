package me.upperlevel.verifier.conn;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.Getter;
import me.upperlevel.verifier.packetlib.Connection;
import me.upperlevel.verifier.packetlib.ServerConnectionInitializer;
import me.upperlevel.verifier.packetlib.PacketManager;

import java.nio.charset.StandardCharsets;

public class Server {
    private final int port;
    @Getter
    private PacketManager<ClientHandler> packetManager = new PacketManager<>(ClientHandler.class);

    public Server(int port) {
        this.port = port;
        Thread.currentThread().setName("Verifier - Server");
        packetManager.addListener(new ServerListener());
    }

    public Server() {
        this(25566);
    }

    public void start() throws InterruptedException {
        System.out.println("Using: " + InternalLoggerFactory.getDefaultFactory().getClass().getSimpleName());
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler("Netty", LogLevel.INFO))
                    .childHandler(new ServerConnectionInitializer<>(this::onConnect, packetManager, true));

            registerDefHandlers();

            // Start the server.
            ChannelFuture f = b.bind(port).sync();
            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void registerDefHandlers() {
        packetManager.register("str", String.class, (String in) -> toString().getBytes(StandardCharsets.UTF_8), (byte[] in) -> new String(in, StandardCharsets.UTF_8));
    }

    public ClientHandler onConnect(Connection connection) {
        ClientHandler handler = new ClientHandler(connection);
        return handler;
    }
}

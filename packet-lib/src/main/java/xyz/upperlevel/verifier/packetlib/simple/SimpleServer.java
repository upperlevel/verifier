package xyz.upperlevel.verifier.packetlib.simple;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import xyz.upperlevel.verifier.packetlib.PacketManager;
import xyz.upperlevel.verifier.packetlib.SimpleConnectionOptions;

public class SimpleServer {
    @Getter
    private final int port;
    @Getter
    private final PacketManager packetManager;
    @Getter
    private final PacketExecutorManager executorManager;
    @Getter
    private final SslHandler ssl;

    private final int bossThreadNubmer, workerThreadNumber;

    private EventLoopGroup bossGroup, workerGroup;

    private ChannelFuture channel;

    public SimpleServer(int port, int bossThreadNumber, int workerThreadNumber, SimpleConnectionOptions options, SslHandler ssl) {
        this.port = port;
        packetManager = new PacketManager(PacketManager.SideType.SERVER, options);
        this.ssl = ssl;
        executorManager = new PacketExecutorManager(packetManager);
        this.bossThreadNubmer = bossThreadNumber;
        this.workerThreadNumber = workerThreadNumber;
    }

    public SimpleServer(int port, SimpleServerOptions options) {
        this(port, options.bossThreadNumber, options.workerThreadNumber, options.connectionOptions, options.ssl);
    }

    public SimpleServer(int port) {
        this(port, SimpleServerOptions.DEFAULT);
    }

    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(bossThreadNubmer);
        workerGroup = new NioEventLoopGroup(workerThreadNumber);
        try {
            ServerBootstrap b = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            if(ssl != null)
                                pipeline.addLast("ssl", ssl);

                            packetManager.initializer.setup(channel);
                            pipeline.addLast("handler", executorManager.createCaller());
                        }
                    });

            // Start the server.
            channel = b.bind(port).sync();
            channel.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully().await();
            workerGroup.shutdownGracefully().await();
        }
        System.out.println("Server closed");
    }

    public void shutdown() {
        System.out.println("Shutting down boss group");
        try {
            bossGroup.shutdownGracefully().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Shutting down worker group");
        try {
            workerGroup.shutdownGracefully().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AllArgsConstructor
    @Builder
    public static class SimpleServerOptions {
        public static final SimpleServerOptions DEFAULT = builder().build();

        public final int bossThreadNumber;
        public final int workerThreadNumber;
        public final SimpleConnectionOptions connectionOptions;
        public final SslHandler ssl;

        public static class SimpleServerOptionsBuilder {
            private int bossThreadNumber = 0;
            private int workerThreadNumber = 1;
            private SimpleConnectionOptions connectionOptions = SimpleConnectionOptions.DEFAULT;
            public SslHandler ssl = null;
        }
    }
}

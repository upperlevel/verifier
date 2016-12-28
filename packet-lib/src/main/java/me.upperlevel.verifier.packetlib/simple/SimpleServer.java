package me.upperlevel.verifier.packetlib.simple;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;
import me.upperlevel.verifier.packetlib.PacketManager;
import me.upperlevel.verifier.packetlib.SimpleConnectionOptions;

import static me.upperlevel.verifier.packetlib.PacketManager.SideType.SERVER;

public class SimpleServer {
    @Getter
    private final int port;
    @Getter
    private final PacketManager packetManager;
    @Getter
    private final PacketExecutorManager executorManager;
    private final int bossThreadNubmer, workerThreadNumber;

    public SimpleServer(int port, int bossThreadNumber, int workerThreadNumber, SimpleConnectionOptions options) {
        this.port = port;
        packetManager = new PacketManager(SERVER, options);
        executorManager = new PacketExecutorManager(packetManager);
        this.bossThreadNubmer = bossThreadNumber;
        this.workerThreadNumber = workerThreadNumber;
    }

    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(bossThreadNubmer);
        EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreadNumber);
        try {
            ServerBootstrap b = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override protected void initChannel(SocketChannel channel) throws Exception {
                            packetManager.initializer.setup(channel);
                            channel.pipeline().addLast("handler", executorManager.createCaller());
                        }
                    });

            // Start the server.
            ChannelFuture f = b.bind(port).sync().channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

package xyz.upperlevel.verifier.packetlib.simple;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import xyz.upperlevel.verifier.packetlib.PacketManager;
import xyz.upperlevel.verifier.packetlib.SimpleConnectionOptions;

import java.util.function.Consumer;

public class SimpleClient {
    @Getter
    private final int port;
    @Getter
    private final String host;
    @Getter
    private final int threadsNumber;

    @Getter
    private Channel channel;
    private EventLoopGroup group = null;

    @Getter
    private final PacketManager packetManager;
    @Getter
    private final PacketExecutorManager executorManager;

    public SimpleClient(String host, int port, int threadsNumber, SimpleConnectionOptions connOptions) {
        this.port = port;
        this.host = host;
        packetManager = new PacketManager(PacketManager.SideType.CLIENT, connOptions);
        this.threadsNumber = threadsNumber;
        executorManager = new PacketExecutorManager(packetManager);
    }

    public SimpleClient(String host, int port, SimpleConnectionOptions options) {
        this(host, port, 0, options);
    }

    public SimpleClient(String host, int port, int threadsNumber) {
        this(host, port, threadsNumber, SimpleConnectionOptions.DEFAULT);
    }

    public SimpleClient(String host, int port) {
        this(host, port, 0, SimpleConnectionOptions.DEFAULT);
    }

    public void start() throws InterruptedException {
        group = new NioEventLoopGroup(threadsNumber);

        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override protected void initChannel(SocketChannel channel) throws Exception {
                        packetManager.initializer.setup(channel);
                        channel.pipeline().addLast("handler", executorManager.createCaller());
                    }
                });
        final ChannelFuture future = bootstrap.connect(host, port);
        future.await();
        channel = future.sync().channel();
    }

    public <T> SimpleClient manage(Class<T> packetClazz, Consumer<T> callback) {
        executorManager.register(packetClazz, callback);
        return this;
    }

    public <T> SimpleClient manage(Class<T> packetClazz, PacketExecutorManager.Executor<T> callback) {
        executorManager.register(packetClazz, callback);
        return this;
    }

    public void shutdown() {
        if(group == null)
            throw new IllegalStateException("Client not initialized");
        group.shutdownGracefully();
    }
}

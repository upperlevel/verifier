package xyz.upperlevel.verifier.packetlib.simple;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
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

    private final SslHandler ssl;

    public SimpleClient(String host, int port, int threadsNumber, SimpleConnectionOptions connOptions, SslHandler ssl) {
        this.port = port;
        this.host = host;
        packetManager = new PacketManager(PacketManager.SideType.CLIENT, connOptions);
        this.threadsNumber = threadsNumber;
        this.ssl = ssl;
        executorManager = new PacketExecutorManager(packetManager);
    }

    public SimpleClient(String host, int port, SimpleClientOptions options) {
        this(host, port, options.threadNumber, options.connectionOptions, options.ssl);
    }

    public SimpleClient(String host, int port) {
        this(host, port, SimpleClientOptions.DEFAULT);
    }

    public void start() throws InterruptedException {
        group = new NioEventLoopGroup(threadsNumber);

        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();

                        if(ssl != null)
                            pipeline.addLast(ssl);
                        packetManager.initializer.setup(channel);
                        pipeline.addLast("handler", executorManager.createCaller());
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

    @AllArgsConstructor
    @Builder
    public static class SimpleClientOptions {
        public static final SimpleClientOptions DEFAULT = builder().build();

        public final int threadNumber;
        public final SimpleConnectionOptions connectionOptions;
        public final SslHandler ssl;

        public static class SimpleClientOptionsBuilder {
            private int threadNumber = 0;
            private SimpleConnectionOptions connectionOptions = SimpleConnectionOptions.DEFAULT;
            public SslHandler ssl = null;
        }
    }
}

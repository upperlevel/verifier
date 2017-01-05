package xyz.upperlevel.verifier.packetlib.simple;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
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
import java.util.function.Function;

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

    private final Function<SocketChannel, SslHandler> ssl;

    public SimpleClient(String host, int port, int threadsNumber, SimpleConnectionOptions connOptions, Function<SocketChannel, SslHandler> ssl) {
        this.port = port;
        this.host = host;
        packetManager = new PacketManager(PacketManager.SideType.CLIENT, connOptions);
        this.threadsNumber = threadsNumber;
        this.ssl = ssl;
        executorManager = new PacketExecutorManager(packetManager);
        group = new NioEventLoopGroup(threadsNumber, r -> {
            return new Thread(r, "Client Thread");
        });
    }

    public SimpleClient(String host, int port, SimpleClientOptions options) {
        this(host, port, options.threadNumber, options.connectionOptions, options.ssl);
    }

    public SimpleClient(String host, int port) {
        this(host, port, SimpleClientOptions.DEFAULT);
    }

    public void start() throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();

                        if (ssl != null)
                            pipeline.addLast(ssl.apply(channel));
                        packetManager.initializer.setup(channel);
                        pipeline.addLast("handler", executorManager.createCaller());
                    }
                });
        final ChannelFuture future = bootstrap.connect(host, port);
        try {
            future.await();
            channel = future.sync().channel();
        } catch (Exception e) {
            group.shutdownGracefully();
            throw e;
        }
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
        if(channel != null) {
            try {
                channel.close().await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            group.shutdownGracefully().await();
        } catch (InterruptedException e) {
            throw new RuntimeException("Error closing The client ThreadGroup", e);
        }
    }

    @AllArgsConstructor
    @Builder
    public static class SimpleClientOptions {
        public static final SimpleClientOptions DEFAULT = builder().build();

        public final int threadNumber;
        public final SimpleConnectionOptions connectionOptions;
        public final Function<SocketChannel, SslHandler> ssl;

        public static class SimpleClientOptionsBuilder {
            private int threadNumber = 0;
            private SimpleConnectionOptions connectionOptions = SimpleConnectionOptions.DEFAULT;
            private Function<SocketChannel, SslHandler> ssl = null;

            public SimpleClientOptionsBuilder sslBB(Function<ByteBufAllocator, SslHandler> ssl) {
                if(ssl == null)
                    this.ssl = null;
                else
                    this.ssl = channel -> ssl.apply(channel.alloc());
                return this;
            }
        }
    }
}

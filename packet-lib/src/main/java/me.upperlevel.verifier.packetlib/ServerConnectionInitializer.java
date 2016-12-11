package me.upperlevel.verifier.packetlib;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import java.util.function.Function;

public class ServerConnectionInitializer<S> extends ChannelInitializer<SocketChannel> {
    private final Function<Connection, S> consumer;
    private final PacketManager<S> packetManager;

    public ServerConnectionInitializer(Function<Connection, S> consumer, PacketManager<S> manager) {
        this.consumer = consumer;
        this.packetManager = manager;
    }


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        /*p.addLast(new ProtobufVarint32FrameDecoder());
        p.addLast(new ProtobufDecoder(Protocol.Assignment.getDefaultInstance()));*/
        p.addLast(new MessageDecoder(packetManager));

        /*p.addLast(new ProtobufVarint32LengthFieldPrepender());
        p.addLast(new ProtobufEncoder());*/
        p.addLast(new MessageEncoder(packetManager));
        Connection<S> connection = new Connection<>(packetManager, new MessageReceiver<S>());
        connection.getHandler().setSender(consumer.apply(connection));
        p.addLast(connection.getHandler());
    }
}

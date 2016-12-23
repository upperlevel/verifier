package me.upperlevel.verifier.packetlib;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;
import me.upperlevel.verifier.packetlib.defs.HandshakeHandler;

import java.util.function.Function;

public class ServerConnectionInitializer<S> extends ChannelInitializer<SocketChannel> {
    private final Function<Connection, S> consumer;
    private final PacketManager<S> packetManager;
    @Getter
    private final boolean server;

    public ServerConnectionInitializer(Function<Connection, S> consumer, PacketManager<S> manager, boolean server) {
        this.consumer = consumer;
        this.packetManager = manager;
        this.server = server;
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

        if(!server)
            p.addLast(new HandshakeHandler(packetManager));

        Connection<S> connection = new Connection<>(packetManager, new MessageReceiver<>());
        connection.getHandler().setSender(consumer.apply(connection));
        p.addLast(connection.getHandler());

        if(server)
            ch.write(packetManager.getHandshake());
    }
}

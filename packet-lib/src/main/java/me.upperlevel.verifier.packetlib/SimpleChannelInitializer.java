package me.upperlevel.verifier.packetlib;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import lombok.Getter;
import me.upperlevel.verifier.packetlib.proto.HandshakePacket;

public class SimpleChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final int maxLength;
    private final int lntBytes;
    private final PacketManager.PacketTypeLength typeBytes;
    private final boolean client;

    @Getter
    protected final PacketManager manager;

    public SimpleChannelInitializer(PacketManager manager) {
        this.manager = manager;
        SimpleConnectionOptions options = manager.getOptions();
        maxLength = options.getMaxPacketSize();
        lntBytes = options.getLengthBytes();
        typeBytes = options.getTypeBytes();
        client = manager.isClient();
    }


    @Override protected void initChannel(SocketChannel channel) throws Exception {
        setup(channel);
    }

    public void setup(SocketChannel channel) {
        ChannelPipeline p = channel.pipeline();

        p.addLast("deframer", new LengthFieldBasedFrameDecoder(maxLength, 0, lntBytes, 0, lntBytes));
        p.addLast("decoder", new AnyPacket.TypeDecoder(typeBytes));
        p.addLast("demux", manager.createDecoder());

        p.addLast("framer", new LengthFieldPrepender(lntBytes));
        p.addLast("encoder", new AnyPacket.TypeEncoder(typeBytes));
        p.addLast("mux", manager.createEncoder());

        if(client)
            p.addLast(new HandshakeHandler());
    }


    private class HandshakeHandler extends SimpleChannelInboundHandler<HandshakePacket> {

        @Override
        protected void channelRead0(ChannelHandlerContext context, HandshakePacket handshake) throws Exception {
            manager.onHandshake(handshake);
        }
    }
}

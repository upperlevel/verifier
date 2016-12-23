package me.upperlevel.verifier.packetlib.defs;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.upperlevel.verifier.packetlib.PacketManager;

public class HandshakeHandler extends SimpleChannelInboundHandler<HandshakePacket> {
    private final PacketManager manager;

    public HandshakeHandler(PacketManager manager) {
        this.manager = manager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, HandshakePacket packet) throws Exception {
        manager.setHandshake(packet);
    }
}

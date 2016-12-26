package me.upperlevel.verifier.client.conn;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.upperlevel.verifier.client.conn.proto.MessagePacket;

public class ServerHandler extends SimpleChannelInboundHandler<MessagePacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessagePacket message) throws Exception {
        System.out.println(message.getMessage());
    }
}

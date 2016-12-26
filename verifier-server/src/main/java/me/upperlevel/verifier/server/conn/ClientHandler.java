package me.upperlevel.verifier.server.conn;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import me.upperlevel.verifier.server.conn.proto.MessagePacket;

public class ClientHandler extends SimpleChannelInboundHandler<MessagePacket>{

    private final static ChannelGroup channels = new DefaultChannelGroup("channels", GlobalEventExecutor.INSTANCE);

    @Override
    public void handlerAdded(ChannelHandlerContext context) {
        Channel incoming = context.channel();
        final String str = "[SERVER] " + incoming.remoteAddress() + " joined!";
        System.out.println(str);
        for(Channel channel : channels)
            channel.writeAndFlush(str);
        channels.add(incoming);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext context) {
        Channel incoming = context.channel();
        final String str = "[SERVER] " + incoming.remoteAddress() + " quitted!";
        System.out.println(str);
        channels.remove(incoming);
        for(Channel channel : channels)
            channel.writeAndFlush(str);
    }

    @Override protected void channelRead0(ChannelHandlerContext context, MessagePacket s) throws Exception {
        Channel incoming = context.channel();
        final String str =  "[" + incoming.remoteAddress() + "] " + s.getMessage() + "";
        System.out.println(str);
        for(Channel channel : channels) {
            if(channel != incoming)
                channel.writeAndFlush(new MessagePacket(str));
        }
    }
}

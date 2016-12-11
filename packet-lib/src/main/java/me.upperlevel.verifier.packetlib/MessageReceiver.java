package me.upperlevel.verifier.packetlib;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AccessLevel;
import lombok.Setter;

public class MessageReceiver<S> extends SimpleChannelInboundHandler<Object> {
    private Channel channel;
    //private Object resp;
    //BlockingQueue<Object> resps = new LinkedBlockingQueue<>();
    @Setter(AccessLevel.PACKAGE)
    private PacketManager<S> manager;
    private S sender;

    /*public Object sendRequest(Object object) {
        Protocol.Assignment req = Protocol.Assignment.newBuilder()
                .setRequestMsg("From Client").build();

        // Send request
        channel.writeAndFlush(req);

        // Now wait for response from server
        boolean interrupted = false;
        for (;;) {
            try {
                resp = resps.take();
                break;
            } catch (InterruptedException ignore) {
                interrupted = true;
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }

        return resp;
    }*/

    public void send(Object object) {
        channel.writeAndFlush(object);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        manager.onMessageReceive(sender, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public void setSender(S sender) {
        this.sender = sender;
    }
}

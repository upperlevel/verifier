package xyz.upperlevel.verifier.server;

import io.netty.channel.ChannelHandlerContext;
import xyz.upperlevel.verifier.proto.protobuf.AssignmentPacket;
import xyz.upperlevel.verifier.proto.protobuf.ErrorPacket;
import xyz.upperlevel.verifier.proto.protobuf.LoginPacket;
import xyz.upperlevel.verifier.proto.protobuf.TimePacket;

import static xyz.upperlevel.verifier.server.Main.handlers;

public class ConnListener {
    public void onConnect(ChannelHandlerContext context) {
        handlers().register(context.channel()).onConnect();
    }

    public void onDisconnect(ChannelHandlerContext context) {
        handlers().remove(context.channel()).onDisconnect();
    }

    public void onAssignment(ChannelHandlerContext context, AssignmentPacket.Assignment packet) {
        log("Assignment");
        handlers().get(context.channel()).onAssignment(packet);
    }

    public void onError(ChannelHandlerContext context, ErrorPacket.Error packet) {
        log("Error");
        handlers().get(context.channel()).onError(packet);
    }

    public void onLogin(ChannelHandlerContext context, LoginPacket.Login packet) {
        log("Login");
        handlers().get(context.channel()).onLogin(packet);
    }

    public void onTime(ChannelHandlerContext context, TimePacket.Time packet) {
        log("Time");
        handlers().get(context.channel()).onTime(packet);
    }

    private void log(String s) {
        System.out.println("->" + s);
    }
}

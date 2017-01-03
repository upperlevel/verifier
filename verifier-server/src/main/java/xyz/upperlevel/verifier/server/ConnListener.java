package xyz.upperlevel.verifier.server;

import io.netty.channel.ChannelHandlerContext;
import xyz.upperlevel.verifier.proto.*;

import static xyz.upperlevel.verifier.server.Main.handlers;

public class ConnListener {
    public void onConnect(ChannelHandlerContext context) {
        handlers().register(context.channel()).onConnect();
    }

    public void onDisconnect(ChannelHandlerContext context) {
        handlers().remove(context.channel()).onDisconnect();
    }

    public void onAssignment(ChannelHandlerContext context, AssignmentPacket packet) {
        log("Assignment");
        handlers().get(context.channel()).onAssignment(packet);
    }

    public void onError(ChannelHandlerContext context, ErrorPacket packet) {
        log("Error");
        handlers().get(context.channel()).onError(packet);
    }

    public void onExeRequest(ChannelHandlerContext context, ExerciseTypePacket packet) {
        log("ExeTypeRequest");
        handlers().get(context.channel()).onExeRequest(packet);
    }

    public void onLogin(ChannelHandlerContext context, LoginPacket packet) {
        log("Login");
        handlers().get(context.channel()).onLogin(packet);
    }

    public void onTime(ChannelHandlerContext context, TimePacket packet) {
        log("Time");
        handlers().get(context.channel()).onTime(packet);
    }

    private void log(String s) {
        System.out.println("->" + s);
    }
}

package me.upperlevel.verifier.server.conn;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import me.upperlevel.verifier.packetlib.PacketManager;

public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    private final PacketManager manager;

    public ServerInitializer(PacketManager manager) {
        this.manager = manager;
    }

    @Override protected void initChannel(SocketChannel channel) throws Exception {
        manager.initializer.setup(channel);
        ChannelPipeline p = channel.pipeline();

        p.addLast("handler", new ClientHandler());
    }
}

package xyz.upperlevel.verifier.server;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

public class ClientManager {
    protected final Map<Channel, ClientHandler> clients = new HashMap<>();

    public void register(ClientHandler handler) {
        clients.put(handler.getChannel(), handler);
    }

    public ClientHandler register(Channel channel) {
        ClientHandler handler = new ClientHandler(channel);
        register(handler);
        return handler;
    }

    public ClientHandler remove(ClientHandler handler) {
        return remove(handler.getChannel());
    }

    public ClientHandler remove(Channel handler) {
        return clients.remove(handler);
    }

    public ClientHandler get(Channel channel) {
        return clients.get(channel);
    }
}

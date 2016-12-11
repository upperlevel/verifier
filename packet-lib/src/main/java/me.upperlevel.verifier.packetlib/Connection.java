package me.upperlevel.verifier.packetlib;

import lombok.Getter;

public class Connection<S> {
    @Getter
    protected final PacketManager<S> packetManager;
    @Getter
    private final MessageReceiver<S> handler;

    public Connection(PacketManager<S> manager, MessageReceiver<S> handler) {
        this.packetManager = manager;
        this.handler = handler;
    }

    public void send(Object packet) {
        handler.send(packet);
    }
}

package me.upperlevel.verifier.conn;

import me.upperlevel.verifier.packetlib.Connection;

public class ServerHandler {
    private final Connection connection;

    public ServerHandler(Connection connection) {
        this.connection = connection;
    }
}

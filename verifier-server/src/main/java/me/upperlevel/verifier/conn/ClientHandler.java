package me.upperlevel.verifier.conn;

import me.upperlevel.verifier.packetlib.Connection;

public class ClientHandler {
    private final Connection connection;
    public ClientHandler(Connection connection) {
        this.connection = connection;
    }
}

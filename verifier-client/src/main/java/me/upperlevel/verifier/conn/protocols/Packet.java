package me.upperlevel.verifier.conn.protocols;

import lombok.Getter;

public abstract class Packet {
    @Getter
    private final String name;


    protected Packet(String name) {
        this.name = name;
    }

    public abstract byte[] serialize();
}

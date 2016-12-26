package me.upperlevel.verifier.client.conn.proto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.upperlevel.verifier.packetlib.PacketHandler;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@AllArgsConstructor
public class MessagePacket {
    public static final PacketHandler HANDLER = new MessagePacketHandler();

    public static final Charset CHARSET = StandardCharsets.UTF_8;

    @Getter
    private final String message;

    private static class MessagePacketHandler extends PacketHandler<MessagePacket> {
        protected MessagePacketHandler() {
            super("message", MessagePacket.class);
        }

        @Override public MessagePacket decode(byte[] encoded) {
            return new MessagePacket(new String(encoded, CHARSET));
        }

        @Override public byte[] encode(MessagePacket decoded) {
            return decoded.getMessage().getBytes(CHARSET);
        }
    }
}

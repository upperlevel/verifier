package me.upperlevel.verifier.packetlib.defs;

import me.upperlevel.verifier.packetlib.PacketManager;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class HandshakePacket {
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final byte DIVIDER = (byte)'\0';

    public static final PacketManager.PacketHandler<HandshakePacket> HANDLER = new HandshakePacketHandler();

    public final String[] packets;

    public HandshakePacket(Collection<String> packets) {
        this.packets = packets.toArray(new String[packets.size()]);
    }

    public HandshakePacket(String[] packets) {
        this.packets = Arrays.copyOf(packets, packets.length);
    }

    private static class HandshakePacketHandler extends PacketManager.PacketHandler<HandshakePacket> {
        public HandshakePacketHandler() {
            super("_handshake", HandshakePacket.class);
        }

        @Override public byte[] encode(HandshakePacket decoded) {
            ByteBuffer builder = ByteBuffer.allocate(decoded.packets.length * 128);

            for(String str : decoded.packets)
                builder
                        .put(str.getBytes(CHARSET))
                        .put(DIVIDER);
            return read(builder);
        }

        @Override public HandshakePacket decode(byte[] encoded) {
            List<String> res = new ArrayList<>();
            ByteBuffer builder = ByteBuffer.allocate(128);
            for(byte b : encoded)
                if(b != DIVIDER)
                    builder.put(b);
                else
                    res.add(new String(readAndReset(builder), CHARSET));
            if(builder.hasRemaining())
                res.add(new String(readAndReset(builder), CHARSET));
            return new HandshakePacket(res);
        }

        private byte[] readAndReset(ByteBuffer builder) {
            byte[] read = read(builder);
            builder.reset();
            return read;
        }

        private byte[] read(ByteBuffer buffer) {
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return bytes;
        }
    }
}

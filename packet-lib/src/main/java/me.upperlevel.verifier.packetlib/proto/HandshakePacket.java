package me.upperlevel.verifier.packetlib.proto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.upperlevel.verifier.packetlib.PacketHandler;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class HandshakePacket {
    public static final PacketHandler<HandshakePacket> HANDLER = new HandshakePacketHandler();

    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final float MAX_BYTES_PER_CHAR = CHARSET.newEncoder().maxBytesPerChar();
    public static final String PKT_NAME = "handshake";
    public static final byte DIVIDER = '\0';

    @Getter
    private List<String> packets;

    private static class HandshakePacketHandler extends PacketHandler<HandshakePacket> {
        protected HandshakePacketHandler() {
            super(PKT_NAME, HandshakePacket.class);
        }

        @Override public HandshakePacket decode(byte[] encoded) {
            List<String> packets = new ArrayList<>(256);
            ByteBuffer buffer = ByteBuffer.allocate((int) (100 * MAX_BYTES_PER_CHAR));

            for(byte b : encoded) {
                if(b == DIVIDER) {
                    if(buffer.remaining() > 0) {
                        byte[] bytes = read(buffer);
                        if(bytes.length > 0)
                            packets.add(new String(bytes, CHARSET));
                        buffer.clear();
                    } else buffer.put(b);
                }
            }

            byte[] bytes = read(buffer);
            if(bytes.length > 0)
                packets.add(new String(bytes, CHARSET));

            return new HandshakePacket(packets);
        }

        @Override public byte[] encode(HandshakePacket decoded_raw) {
            List<String> decoded = decoded_raw.packets;

            int sum = 0;
            for(String str : decoded)
                sum += str.length() *MAX_BYTES_PER_CHAR + 1;

            ByteBuffer buffer = ByteBuffer.allocate(sum);

            final int size = decoded.size();
            for (int i = 0; i < size; i++) {
                if(i == 0)
                    buffer.put(DIVIDER);
                buffer.put(decoded.get(i).getBytes(CHARSET));
            }

            return read(buffer);
        }

        public static byte[] read(ByteBuffer buffer) {
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return bytes;
        }
    }
}

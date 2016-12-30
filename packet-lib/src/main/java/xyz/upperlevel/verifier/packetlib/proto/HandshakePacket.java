package xyz.upperlevel.verifier.packetlib.proto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import xyz.upperlevel.verifier.packetlib.PacketHandler;
import xyz.upperlevel.verifier.packetlib.utils.ByteConvUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@EqualsAndHashCode(of = "packets")
public class HandshakePacket {
    public static final PacketHandler<HandshakePacket> HANDLER = new HandshakePacketHandler();

    public static final String PKT_NAME = "handshake";

    @Getter
    private final List<String> packets;

    private static class HandshakePacketHandler extends PacketHandler<HandshakePacket> {
        protected HandshakePacketHandler() {
            super(PKT_NAME, HandshakePacket.class);
        }

        @Override public HandshakePacket decode(byte[] encoded) {
            return new HandshakePacket(Arrays.asList(ByteConvUtils.readStringArray(ByteBuffer.wrap(encoded))));
        }

        @Override public byte[] encode(HandshakePacket decoded_raw) {
            return ByteConvUtils.writeStringArray(decoded_raw.getPackets());
        }

        public static byte[] read(ByteBuffer buffer) {
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return bytes;
        }
    }

    @Override
    public String toString() {
        return packets.toString();
    }

    public static HandshakePacket fromHandlers(List<PacketHandler<?>> handlers) {
        return new HandshakePacket(handlers.stream().map(PacketHandler::getName).collect(Collectors.toList()));
    }
}

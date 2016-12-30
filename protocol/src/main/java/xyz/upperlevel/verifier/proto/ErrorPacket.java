package xyz.upperlevel.verifier.proto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import xyz.upperlevel.verifier.packetlib.PacketHandler;
import xyz.upperlevel.verifier.packetlib.exceptions.IllegalPacketException;
import xyz.upperlevel.verifier.packetlib.utils.ByteConvUtils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@AllArgsConstructor
@EqualsAndHashCode
public class ErrorPacket {
    public static final PacketHandler<ErrorPacket> HANDLER = new ErrorPacketHandler();

    public static final Charset CHARSET = ByteConvUtils.DEF_CHARSET;

    @Getter
    private final ErrorType type;

    @Getter
    private final String message;

    private static class ErrorPacketHandler extends PacketHandler<ErrorPacket> {
        protected ErrorPacketHandler() {
            super("error", ErrorPacket.class);
        }

        @Override public ErrorPacket decode(byte[] encoded) throws IllegalPacketException {
            return new ErrorPacket(
                    ErrorType.get((int)encoded[0]),
                    new String(encoded, 1, encoded.length - 1, CHARSET)
            );
        }

        @Override public byte[] encode(ErrorPacket decoded) {
            byte[] mess_raw = decoded.message.getBytes(CHARSET);
            return ByteBuffer.allocate(1 + mess_raw.length)
                    .put((byte)decoded.type.getId())
                    .put(mess_raw)
                    .array();
        }
    }
}

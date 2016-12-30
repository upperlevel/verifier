package xyz.upperlevel.verifier.proto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import xyz.upperlevel.verifier.packetlib.PacketHandler;
import xyz.upperlevel.verifier.packetlib.exceptions.IllegalPacketException;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@EqualsAndHashCode(of = {"type", "data"})
public class ExerciseData {
    public static final PacketHandler<ExerciseData> HANDLER = new MessagePacketHandler();
    public static final byte DIVIDER = '\0';

    public static final Charset CHARSET = StandardCharsets.UTF_8;

    @Getter
    private final String type;

    @Getter
    private final byte[] data;

    private byte[] encoded = null;

    public ExerciseData(String type, byte[] data) {
        this.type = type;
        this.data = data;
    }


    public byte[] encode() {
        if (encoded == null) {
            byte[] type_raw = type.getBytes(CHARSET);
            encoded = ByteBuffer.allocate(type_raw.length + 1 + data.length)
                    .put(type_raw)
                    .put(DIVIDER)
                    .put(data)
                    .array();
        }
        return encoded;
    }

    public static ExerciseData decode(byte[] encoded) throws IllegalPacketException {
        String str = null;
        int i = 0;
        for(; i < encoded.length; i++) {
            if(encoded[i] == DIVIDER) {
                str = new String(encoded, 0, i, CHARSET);
                break;
            }
        }
        if(str == null)
            throw new IllegalPacketException("String not ended in Excercize Packet");
        i++;
        byte[] data = Arrays.copyOfRange(encoded, i, encoded.length);
        return new ExerciseData(
                str,
                data
        );
    }




    private static class MessagePacketHandler extends PacketHandler<ExerciseData> {
        protected MessagePacketHandler() {
            super("exercise", ExerciseData.class);
        }

        @Override public byte[] encode(ExerciseData decoded) {
            return decoded.encode();
        }

        @Override public ExerciseData decode(byte[] encoded) throws IllegalPacketException {
            return ExerciseData.decode(encoded);
        }
    }
}
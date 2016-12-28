package me.upperlevel.verifier.proto;

import lombok.Getter;
import me.upperlevel.verifier.packetlib.PacketHandler;
import me.upperlevel.verifier.packetlib.exceptions.IllegalPacketException;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ExerciseData {
    public static final PacketHandler<ExerciseData> HANDLER = new MessagePacketHandler();
    public static final byte DIVIDER = '\0';

    public static final Charset CHARSET = StandardCharsets.UTF_8;

    @Getter
    private final String exercise_type;

    @Getter
    private final byte[] exercize_data;

    private byte[] encoded = null;

    public ExerciseData(String exercise_type, byte[] exercize_data) {
        this.exercise_type = exercise_type;
        this.exercize_data = exercize_data;
    }


    public byte[] encode() {
        if (encoded == null) {
            byte[] type_raw = exercise_type.getBytes(CHARSET);
            encoded = ByteBuffer.allocate(type_raw.length + 1 + exercize_data.length)
                    .put(type_raw)
                    .put(DIVIDER)
                    .put(exercize_data)
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
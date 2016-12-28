package me.upperlevel.verifier.proto;

import lombok.Getter;
import me.upperlevel.verifier.packetlib.PacketHandler;
import me.upperlevel.verifier.packetlib.exceptions.IllegalPacketException;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AssignmentPacket {
    public static final PacketHandler<AssignmentPacket> HANDLER = new AssignmentPacketHandler();
    public static final byte DIVIDER = '\0';

    public static final Charset CHARSET = StandardCharsets.UTF_8;



    @Getter
    private final List<ExerciseData> exercises;

    private byte[] encoded = null;

    public AssignmentPacket(List<ExerciseData> exercises) {
        this.exercises = exercises;
    }


    public byte[] encode() {
        if (encoded == null) {//Oracle's developers are reealy lazy #ByteStream
            byte[][] ph1 = new byte[exercises.size()][];
            for (int i = 0; i < exercises.size(); i++) {
                byte[] enc = exercises.get(i).encode();
                enc = ByteBuffer.allocate(4 + enc.length)
                        .putInt(enc.length)
                        .put(enc)
                        .array();
                ph1[i] = enc;
            }
            encoded = new byte[count(ph1)];
            int index = 0;

            for(byte[] b : ph1) {
                System.arraycopy(
                        b,
                        0,
                        encoded,
                        index,
                        b.length
                );
                index += b.length;
            }
        }
        return encoded;
    }

    private int count(byte[][] arr) {
        int sum = 0;
        for(byte[] b : arr)
            sum += b.length;
        return sum;
    }

    public static AssignmentPacket decode(byte[] encoded) throws IllegalPacketException {
        ByteBuffer in = ByteBuffer.wrap(encoded);
        List<ExerciseData> exercises = new ArrayList<>();
        while(in.remaining() > 0) {
            byte[] data = new byte[in.getInt()];
            in.get(data);
            exercises.add(ExerciseData.decode(data));
        }
        return new AssignmentPacket(exercises);
    }




    private static class AssignmentPacketHandler extends PacketHandler<AssignmentPacket> {
        protected AssignmentPacketHandler() {
            super("assignment", AssignmentPacket.class);
        }

        @Override public byte[] encode(AssignmentPacket decoded) {
            return decoded.encode();
        }

        @Override public AssignmentPacket decode(byte[] encoded) throws IllegalPacketException {
            return AssignmentPacket.decode(encoded);
        }
    }
}

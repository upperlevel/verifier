package xyz.upperlevel.verifier.proto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import xyz.upperlevel.verifier.packetlib.PacketHandler;
import xyz.upperlevel.verifier.packetlib.exceptions.IllegalPacketException;
import xyz.upperlevel.verifier.packetlib.utils.ByteConvUtils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EqualsAndHashCode(of= {"id", "exercises"})
public class AssignmentPacket {
    public static final PacketHandler<AssignmentPacket> HANDLER = new AssignmentPacketHandler();
    public static final byte SEPARATOR = '\0';

    public static final Charset CHARSET = StandardCharsets.UTF_8;

    @Getter
    private final String id;

    @Getter
    private List<ExerciseData> exercises;

    private byte[] encoded = null;

    public AssignmentPacket(String id, List<ExerciseData> exercises) {
        this.id = id;
        this.exercises = exercises;
    }

    public void setExercises(List<ExerciseData> exercises) {
        this.exercises = exercises;
    }


    public byte[] encode() {
        if (encoded == null) {//Oracle's developers are reealy lazy #ByteStream
            byte[][] ph1 = new byte[exercises.size() + 1][];
            int bcount = 0;

            {//id
                ph1[0] = id.getBytes(CHARSET);

                byte[] encod = Arrays.copyOf(ph1[0], ph1[0].length + 1);
                encod[encod.length - 1] = SEPARATOR;
                ph1[0] = encod;

                bcount += ph1[0].length;
            }

            for (int i = 0; i < exercises.size(); i++) {
                byte[] enc = exercises.get(i).encode();
                ph1[i + 1] = ByteBuffer.allocate(4 + enc.length)
                        .putInt(enc.length)
                        .put(enc)
                        .array();
                bcount += ph1[i + 1].length;
            }

            encoded = new byte[bcount];
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

    public static AssignmentPacket decode(byte[] encoded) throws IllegalPacketException {
        ByteBuffer in = ByteBuffer.wrap(encoded);

        final String id = ByteConvUtils.readString(in, CHARSET, SEPARATOR);

        List<ExerciseData> exercises = new ArrayList<>();
        while(in.remaining() > 0) {
            byte[] data = new byte[in.getInt()];
            in.get(data);
            exercises.add(ExerciseData.decode(data));
        }
        return new AssignmentPacket(id, exercises);
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

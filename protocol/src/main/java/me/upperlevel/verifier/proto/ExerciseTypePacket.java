package me.upperlevel.verifier.proto;

import lombok.Getter;
import me.upperlevel.verifier.packetlib.PacketHandler;
import me.upperlevel.verifier.packetlib.exceptions.IllegalPacketException;
import me.upperlevel.verifier.packetlib.utils.ByteConvUtils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static me.upperlevel.verifier.packetlib.utils.ByteConvUtils.readAll;
import static me.upperlevel.verifier.packetlib.utils.ByteConvUtils.readString;

public class ExerciseTypePacket {
    public static final PacketHandler<ExerciseTypePacket> HANDLER = new TestTypePacketHandler();

    private static final byte[] EMPTY_DATA = {};

    public static final Charset CHARSET = ByteConvUtils.DEF_CHARSET;
    public static final byte SEPARATOR = ByteConvUtils.DEF_SEPARATOR;

    @Getter
    private final String name;

    @Getter
    private final byte[] fileData;

    public ExerciseTypePacket(String name) {
        this(name, EMPTY_DATA);
    }

    public ExerciseTypePacket(String name, byte[] data) {
        this.name = name;
        this.fileData = data;
    }


    private static class TestTypePacketHandler extends PacketHandler<ExerciseTypePacket> {
        protected TestTypePacketHandler() {
            super("ex_type", ExerciseTypePacket.class);
        }

        @Override public ExerciseTypePacket decode(byte[] encoded) throws IllegalPacketException {
            ByteBuffer buffer = ByteBuffer.wrap(encoded);

            return new ExerciseTypePacket(
                    readString(buffer, CHARSET, SEPARATOR),
                    readAll(buffer)
            );
        }

        @Override public byte[] encode(ExerciseTypePacket decoded) {
            byte[] user_raw = decoded.name.getBytes(CHARSET);
            if (decoded.fileData.length <= 0)
                return user_raw;

            return ByteBuffer.allocate(user_raw.length + 1 + decoded.fileData.length)
                    .put(user_raw)
                    .put(SEPARATOR)
                    .put(decoded.fileData)
                    .array();
        }
    }
}

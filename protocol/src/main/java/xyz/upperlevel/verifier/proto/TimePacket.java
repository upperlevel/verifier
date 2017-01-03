package xyz.upperlevel.verifier.proto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import xyz.upperlevel.verifier.packetlib.PacketHandler;
import xyz.upperlevel.verifier.packetlib.exceptions.IllegalPacketException;

import java.nio.ByteBuffer;
import java.time.LocalTime;

@AllArgsConstructor
@EqualsAndHashCode
public class TimePacket {
    public static final PacketHandler<TimePacket> HANDLER = new TimePacketHandler();

    @Getter
    private final PacketType type;

    @Getter
    private final LocalTime time;

    private static class TimePacketHandler extends PacketHandler<TimePacket> {
        protected TimePacketHandler() {
            super("time", TimePacket.class);
        }

        @Override public TimePacket decode(byte[] encoded) throws IllegalPacketException {
            PacketType type = PacketType.fromId(encoded[0]);
            if(type == PacketType.REQUEST)
                return new TimePacket(PacketType.REQUEST, null);
            else if(type == PacketType.SET) {
                long nanos = ByteBuffer.wrap(encoded, 1, encoded.length - 1).getLong();
                if(nanos < 0)
                    return new TimePacket(PacketType.SET, null);
                return new TimePacket(PacketType.SET, LocalTime.ofNanoOfDay(nanos));
            } else
                throw new IllegalPacketException("Time packet " + type + " not implemented");
        }

        @Override public byte[] encode(TimePacket decoded) {
            if(decoded.getType() == PacketType.REQUEST)
                return new byte[] {(byte) PacketType.REQUEST.id()};
            else if(decoded.getType() == PacketType.SET)
                return ByteBuffer.allocate(1 + Long.BYTES)
                        .put((byte)PacketType.SET.id())
                        .putLong(decoded.time == null ? -1 : decoded.time.toNanoOfDay())
                        .array();
            else
                throw new IllegalStateException("The time packet " + decoded.getType() + " is not implemented in the protocol!");
        }
    }

    public enum PacketType {
        SET, REQUEST;
        private static final PacketType[] values = values();

        public int id() {
            return ordinal();
        }

        public static PacketType fromId(int id) {
            return values[id];
        }
    }
}

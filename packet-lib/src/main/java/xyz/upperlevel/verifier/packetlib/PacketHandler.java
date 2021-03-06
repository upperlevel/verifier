package xyz.upperlevel.verifier.packetlib;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import xyz.upperlevel.verifier.packetlib.exceptions.IllegalPacketException;

import java.util.function.Function;

public abstract class PacketHandler<T> {
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Integer id = null;
    @Getter
    private final String name;
    @Getter
    private final Class<T> handled;

    protected PacketHandler(String name, Class<T> handled) {
        this.name = name;
        this.handled = handled;
    }

    public abstract T decode(byte[] encoded) throws IllegalPacketException;
    public abstract byte[] encode(T decoded);

    public static <P> PacketHandler<P> from(String name, Class<P> clazz, Function<byte[], P> encoder, Function<P, byte[]> decoder) {
        return new PacketHandler<P>(name, clazz) {
            @Override public byte[] encode(P decoded) {
                return decoder.apply(decoded);
            }

            @Override public P decode(byte[] encoded) {
                return encoder.apply(encoded);
            }
        };
    }
}

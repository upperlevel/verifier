package xyz.upperlevel.verifier.proto;

import xyz.upperlevel.verifier.packetlib.PacketHandler;
import xyz.upperlevel.verifier.packetlib.exceptions.IllegalPacketException;

public class PacketTestUtil {

    public static  <T> void test(T packet, PacketHandler<T> handler) {
        try {
            assert handler.decode(handler.encode(packet)).equals(packet);
        } catch (IllegalPacketException e) {
            throw new RuntimeException(e);
        }
    }
}

package xyz.upperlevel.verifier.packetlib.proto;

import junit.framework.TestCase;
import xyz.upperlevel.verifier.packetlib.exceptions.IllegalPacketException;

import java.util.Arrays;

public class HandshakePacketTest extends TestCase{

    public void test() throws IllegalPacketException {
        HandshakePacket packet = new HandshakePacket(
                Arrays.asList(
                        "packet1",
                        "test-packet2",
                        "some other freaking packet!",
                        "idk",
                        "name me!",
                        "I love testing packets",
                        "xD"
                )
        );
        HandshakePacket received = HandshakePacket.HANDLER.decode(HandshakePacket.HANDLER.encode(packet));
        assertEquals(packet, received);
    }
}

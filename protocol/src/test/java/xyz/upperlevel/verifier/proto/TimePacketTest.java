package xyz.upperlevel.verifier.proto;

import java.time.LocalTime;

public class TimePacketTest {
    public void test() {
        PacketTestUtil.test(
                new TimePacket(TimePacket.PacketType.SET, LocalTime.now()),
                TimePacket.HANDLER
        );

        PacketTestUtil.test(
                new TimePacket(TimePacket.PacketType.REQUEST, null),
                TimePacket.HANDLER
        );
    }
}

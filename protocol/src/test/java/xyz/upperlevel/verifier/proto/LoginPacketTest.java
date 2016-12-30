package xyz.upperlevel.verifier.proto;

import junit.framework.TestCase;

public class LoginPacketTest extends TestCase {

    public void test() {
        PacketTestUtil.test(
                new LoginPacket("3H", "Rutayisire Lorenzo", "test password 1.0 improved!".toCharArray()),
                LoginPacket.HANDLER
        );
    }
}

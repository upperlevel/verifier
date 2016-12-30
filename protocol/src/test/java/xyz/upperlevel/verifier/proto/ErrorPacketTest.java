package xyz.upperlevel.verifier.proto;

import junit.framework.TestCase;

public class ErrorPacketTest extends TestCase {

    public void test() {
        PacketTestUtil.test(
                new ErrorPacket(ErrorType.LOGIN_BAD_PASSWORD, "BaD p4sSw0rd"),
                ErrorPacket.HANDLER
        );
    }
}

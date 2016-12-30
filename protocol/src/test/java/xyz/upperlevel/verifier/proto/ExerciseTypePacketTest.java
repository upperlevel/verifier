package xyz.upperlevel.verifier.proto;

import junit.framework.TestCase;

import java.util.Random;

public class ExerciseTypePacketTest extends TestCase {

    public void test() {
        byte[] rand_bytes = new byte[256];
        Random random = new Random();
        random.nextBytes(rand_bytes);
        PacketTestUtil.test(
                new ExerciseTypePacket("test_id", rand_bytes),
                ExerciseTypePacket.HANDLER
        );
    }
}

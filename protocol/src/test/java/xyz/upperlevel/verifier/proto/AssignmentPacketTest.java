package xyz.upperlevel.verifier.proto;

import junit.framework.TestCase;

import java.util.Arrays;

public class AssignmentPacketTest extends TestCase {

    public void test() {
        AssignmentPacket packet = new AssignmentPacket(
                "test1-test2",
                Arrays.asList(
                        new ExerciseData("test", new byte[]{1, 2, 3}),
                        new ExerciseData("tast", new byte[]{3, 4, 5}),
                        new ExerciseData("culi", new byte[]{3, 1, 4, 1, 5})
                )
        );
        PacketTestUtil.test(packet, AssignmentPacket.HANDLER);
    }
}

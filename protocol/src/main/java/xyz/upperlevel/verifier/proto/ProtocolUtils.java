package xyz.upperlevel.verifier.proto;

import xyz.upperlevel.verifier.packetlib.PacketManager;

public class ProtocolUtils {

    public static void registerDefPackets(PacketManager manager) {
        manager.register(
                AssignmentPacket.HANDLER,
                ErrorPacket.HANDLER,
                LoginPacket.HANDLER,
                ExerciseTypePacket.HANDLER,
                TimePacket.HANDLER
        );
    }
}

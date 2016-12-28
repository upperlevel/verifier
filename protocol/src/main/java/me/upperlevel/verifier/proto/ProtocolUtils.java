package me.upperlevel.verifier.proto;

import me.upperlevel.verifier.packetlib.PacketManager;

public class ProtocolUtils {

    public static void registerDefPackets(PacketManager manager) {
        manager.register(
                AssignmentPacket.HANDLER,
                ErrorPacket.HANDLER,
                LoginPacket.HANDLER,
                ExerciseTypePacket.HANDLER
        );
    }
}

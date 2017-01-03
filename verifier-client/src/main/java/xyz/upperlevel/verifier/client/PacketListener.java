package xyz.upperlevel.verifier.client;

import xyz.upperlevel.verifier.proto.ErrorType;
import xyz.upperlevel.verifier.proto.ExerciseData;
import xyz.upperlevel.verifier.proto.TimePacket;

import java.time.LocalTime;
import java.util.List;

public interface PacketListener {
    public void onExerciseType(String exName, byte[] data);

    public void onAsignment(String id, List<ExerciseData> exercises);

    public void onError(ErrorType error, String message);

    public void onTime(TimePacket.PacketType type, LocalTime time);
}

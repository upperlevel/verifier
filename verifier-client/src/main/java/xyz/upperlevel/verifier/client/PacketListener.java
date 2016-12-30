package xyz.upperlevel.verifier.client;

import xyz.upperlevel.verifier.proto.ErrorType;
import xyz.upperlevel.verifier.proto.ExerciseData;

import java.util.List;

public interface PacketListener {
    public void onExerciseType(String exName, byte[] data);

    public void onAsignment(String id, List<ExerciseData> exercises);

    public void onError(ErrorType error, String message);
}

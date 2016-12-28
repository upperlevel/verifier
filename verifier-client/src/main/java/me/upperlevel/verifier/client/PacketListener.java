package me.upperlevel.verifier.client;

import me.upperlevel.verifier.proto.ErrorType;
import me.upperlevel.verifier.proto.ExerciseData;

import java.util.List;

public interface PacketListener {
    public void onExerciseType(String exName, byte[] data);

    public void onAsignment(List<ExerciseData> exercises);

    public void onError(ErrorType error, String message);
}

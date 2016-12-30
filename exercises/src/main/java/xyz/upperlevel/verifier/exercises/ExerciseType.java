package xyz.upperlevel.verifier.exercises;

import lombok.AllArgsConstructor;
import xyz.upperlevel.verifier.proto.ExerciseData;

import java.util.IllegalFormatException;
import java.util.Map;

@AllArgsConstructor
public abstract class ExerciseType<E extends Exercise> {
    public final String type;


    public abstract ExerciseData encodeRequest(E exe);

    public abstract ExerciseData encodeResponse(E exe);

    public abstract Map<String, Object> toYamlRequest(E exe);

    public abstract Map<String, Object> toYamlResponse(E exe);


    public abstract E decodeRequest(byte[] encoded) throws IllegalFormatException;

    public abstract E decodeResponse(byte[] encoded) throws IllegalFormatException;

    public abstract E fromYamlRequest(Map<String, Object> yaml);

    public abstract E fromYamlResponse(Map<String, Object> yaml);
}

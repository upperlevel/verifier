package xyz.upperlevel.verifier.exercises;

import lombok.AllArgsConstructor;
import xyz.upperlevel.verifier.proto.ExerciseData;

import java.util.IllegalFormatException;
import java.util.Map;
import java.util.Random;

@AllArgsConstructor
public abstract class ExerciseType<A extends ExerciseRequest, B extends ExerciseResponse> {
    public final String type;


    public abstract ExerciseData encodeRequest(A exe, Random random);

    public abstract Map<String, Object> toYamlRequest(A exe);

    public abstract A decodeRequest(byte[] encoded) throws IllegalFormatException;

    public abstract A fromYamlRequest(Map<String, Object> yaml);


    public abstract ExerciseData encodeResponse(B exe);

    public abstract Map<String, Object> toYamlResponse(B exe);

    public abstract B decodeResponse(byte[] encoded, A req, Random random) throws IllegalFormatException;

    public abstract B fromYamlResponse(Map<String, Object> yaml, A req);
}

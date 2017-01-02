package xyz.upperlevel.verifier.exercises;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.upperlevel.verifier.proto.ExerciseData;

import java.util.Map;
import java.util.Random;

@AllArgsConstructor
public abstract class ExerciseRequest<E extends ExerciseRequest, R extends ExerciseResponse> {
    @Getter
    private final ExerciseType<E, R> type;

    public abstract R getResponse();

    public ExerciseData encode(Random random) {
        return type.encodeRequest(getThis(), random);
    }


    public Map<String, Object> toYaml() {
        return type.toYamlRequest(getThis());
    }

    @SuppressWarnings("unchecked")
    private E getThis() {
        return (E)this;
    }
}

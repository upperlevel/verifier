package xyz.upperlevel.verifier.exercises;

import javafx.scene.Parent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.upperlevel.verifier.exercises.util.Fraction;
import xyz.upperlevel.verifier.proto.ExerciseData;

import java.util.Map;

@AllArgsConstructor
public abstract class ExerciseResponse<R extends ExerciseRequest, E extends ExerciseResponse> {
    @Getter
    private final ExerciseType<R, E> type;

    @Getter
    private final R parent;

    public abstract Parent getGraphics();

    public abstract Fraction correct();

    public ExerciseData encode() {
        return type.encodeResponse(getThis());
    }


    public Map<String, Object> toYaml() {
        return type.toYamlResponse(getThis());
    }

    @SuppressWarnings("unchecked")
    private E getThis() {
        return (E)this;
    }
}

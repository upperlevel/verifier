package xyz.upperlevel.verifier.exercises;

import javafx.scene.Parent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.upperlevel.verifier.proto.ExerciseData;

import java.util.Map;

@AllArgsConstructor
public abstract class Exercise<E extends Exercise<E>> {
    @Getter
    private final ExerciseType<E> type;

    public abstract Parent getGraphics();

    public ExerciseData encodeRequest() {
        return type.encodeRequest(getThis());
    }

    public ExerciseData encodeResponse() {
        return type.encodeResponse(getThis());
    }

    public Map<String, Object> toYamlRequest() {
        return type.toYamlRequest(getThis());
    }

    public Map<String, Object> toYamlResponse() {
        return type.toYamlResponse(getThis());
    }

    @SuppressWarnings("unchecked")
    private E getThis() {
        return (E)this;
    }
}

package xyz.upperlevel.verifier.exercises.def;

import lombok.EqualsAndHashCode;
import xyz.upperlevel.verifier.exercises.ExerciseRequest;

import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = false)
public class MultipleChoiceExerciseRequest extends ExerciseRequest<MultipleChoiceExerciseRequest, MultipleChoiceExerciseResponse>{
    public boolean multiple;
    public String question;
    public List<String> choices;
    public int limit;

    public Set<Integer> answers;

    public MultipleChoiceExerciseRequest(MultipleChoiceExerciseHandler handler) {
        super(handler);
    }

    @Override
    public MultipleChoiceExerciseResponse getResponse() {
        return new MultipleChoiceExerciseResponse((MultipleChoiceExerciseHandler)getType(), this);
    }

    @Override
    public String toString() {
        return question;
    }
}

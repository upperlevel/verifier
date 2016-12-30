package xyz.upperlevel.verifier.exercises.def;

import javafx.scene.Parent;
import lombok.EqualsAndHashCode;
import xyz.upperlevel.verifier.exercises.Exercise;

import java.util.List;
import java.util.Set;

@EqualsAndHashCode
public class MultipleChoiceExercise extends Exercise<MultipleChoiceExercise> {

    public Boolean multiple;
    public String question;
    public List<String> choices;

    public Set<Integer> answers;

    public MultipleChoiceExercise(MultipleChoiceExerciseHandler handler) {
        super(handler);
    }


    @Override public Parent getGraphics() {
        return null;
    }

    @Override
    public String toString() {
        return  "multiple:" + multiple + ", question:" + question + ", choises:" +choices + ", answers:" + answers;
    }
}

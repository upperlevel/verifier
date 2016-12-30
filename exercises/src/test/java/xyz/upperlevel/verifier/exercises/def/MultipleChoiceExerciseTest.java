package xyz.upperlevel.verifier.exercises.def;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.HashSet;

public class MultipleChoiceExerciseTest extends TestCase {

    public MultipleChoiceExerciseTest(String name) {
        super(name);
    }

    public void test() {
        MultipleChoiceExerciseHandler handler = new MultipleChoiceExerciseHandler();
        {
            MultipleChoiceExercise exercise = new MultipleChoiceExercise(handler);

            exercise.question = "To be or not to be";
            exercise.choices = Arrays.asList("To be", "Not to be");
            exercise.multiple = true;

            assertEquals(
                    handler.decodeRequest(
                            handler.encodeRequest(exercise).getData()
                    ),
                    exercise
            );
            assertEquals(
                    handler.fromYamlRequest(
                            handler.toYamlRequest(exercise)
                    ),
                    exercise
            );
        }
        {
            MultipleChoiceExercise exercise = new MultipleChoiceExercise(handler);

            exercise.answers = new HashSet<>(Arrays.asList(3, 1, 5));

            assertEquals(
                    handler.decodeResponse(
                            handler.encodeResponse(exercise).getData()
                    ),
                    exercise
            );
            assertEquals(
                    handler.fromYamlResponse(
                            handler.toYamlResponse(exercise)
                    ),
                    exercise
            );
        }
    }

}

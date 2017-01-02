package xyz.upperlevel.verifier.exercises.def;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class MultipleChoiceExerciseTest extends TestCase {

    public MultipleChoiceExerciseTest(String name) {
        super(name);
    }

    public void test() {
        MultipleChoiceExerciseHandler handler = new MultipleChoiceExerciseHandler();
        MultipleChoiceExerciseRequest request = new MultipleChoiceExerciseRequest(handler);

        MultipleChoiceExerciseRequest received;
        final long seed = System.currentTimeMillis();
        {


            request.question = "To be or not to be";
            request.choices = Arrays.asList("To be", "Not to be", "Maybe be", "I hate bees");
            request.multiple = true;
            request.limit = 4;

            {
                received = handler.decodeRequest(handler.encodeRequest(request, new Random(seed)).getData());
                assert request.choices.containsAll(received.choices);
                assert request.question.equals(received.question);
                assert request.multiple == received.multiple;
            }
            assertEquals(
                    handler.fromYamlRequest(
                            handler.toYamlRequest(request)
                    ),
                    request
            );
        }
        {
            /*System.out.println("Sent: " + request.choices);
            System.out.println("Received: " + received.choices);*/
            Set<String> choosen = new HashSet<>(Arrays.asList(request.choices.get(1), request.choices.get(3)));

            MultipleChoiceExerciseResponse exercise = new MultipleChoiceExerciseResponse(handler, request);

            exercise.answers = choosen.stream()
                    .map(o -> received.choices.indexOf(o))
                    .collect(Collectors.toSet());

            //System.out.println("choosed: " + choosen + " -> " + exercise.answers);

            {
                MultipleChoiceExerciseResponse exe = handler.decodeResponse(handler.encodeResponse(exercise).getData(), request, new Random(seed));

                //System.out.println("server->" + exe.answers + "->" + exe.answers.stream().map(i -> request.choices.get(i)).collect(Collectors.toList()));

                assert exe.answers.size() == exercise.answers.size();//Just for a litle bit more debugging, lol
                //Do NOT compare the raw answers (numbers) but the choices associated with them
                assert (exe.answers.stream().map(i -> request.choices.get(i)).collect(Collectors.toSet()))
                        .equals(
                                exercise.answers.stream().map(i -> received.choices.get(i)).collect(Collectors.toSet())
                        );
            }
            assertEquals(
                    handler.fromYamlResponse(
                            handler.toYamlResponse(exercise),
                            request
                    ),
                    exercise
            );
        }
    }

}

package xyz.upperlevel.verifier.server.ui.console.commands.assignment;

import org.yaml.snakeyaml.Yaml;
import xyz.upperlevel.commandapi.commands.Command;
import xyz.upperlevel.commandapi.executor.CommandRunner;
import xyz.upperlevel.commandapi.executor.ParamName;
import xyz.upperlevel.verifier.exercises.ExerciseRequest;
import xyz.upperlevel.verifier.exercises.def.MultipleChoiceExerciseHandler;
import xyz.upperlevel.verifier.exercises.def.MultipleChoiceExerciseRequest;
import xyz.upperlevel.verifier.server.assignments.AssignmentRequest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class TestCommand extends Command {
    public TestCommand() {
        super("test", "creates a test assignment");
    }

    @CommandRunner
    public void run(@ParamName("path") String path) {
        File file = new File(path);
        if(file.exists()) {
            System.err.println("The path already exists");
            return;
        }
        AssignmentRequest ass = new AssignmentRequest(Arrays.asList(
                exe(
                        true,
                        "To be or not to be?",
                        2,
                        Arrays.asList("To be", "Not to be"),
                        Collections.singletonList(0)
                ),
                exe(
                        false,
                        "Do you want ted-learning?",
                        4,
                        Arrays.asList("No", "Mayb...No", "ehhhhh No", "Uhmmmm...No", "Lol, no!", "Hahah, no", "Nope", "Nada", "Nain!"),
                        Arrays.asList(0, 4)
                ),
                exe(
                        false,
                        "What is the mass of the sun?",
                        2,
                        Arrays.asList("1.989x10^30", "3.14", "google", "a number", "a final double"),
                        Arrays.asList(0, 4)
                )
        ), "test-id");

        try(FileWriter writer = new FileWriter(file)) {
            new Yaml().dump(ass.toYaml(), writer);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("Test assignment wrote successfully");
    }


    private static ExerciseRequest<?, ?> exe(boolean multi, String question, int limit, List<String> choices, List<Integer> ans) {
        MultipleChoiceExerciseRequest exercise = new MultipleChoiceExerciseRequest(new MultipleChoiceExerciseHandler());
        exercise.multiple = multi;
        exercise.question = question;
        exercise.choices = choices;
        exercise.limit = limit;

        exercise.answers = new HashSet<>(ans);
        return exercise;
    }
}

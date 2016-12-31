package xyz.upperlevel.verifier.server.ui.console.commands;

import org.yaml.snakeyaml.Yaml;
import xyz.upperlevel.commandapi.CommandRunner;
import xyz.upperlevel.commandapi.commands.Command;
import xyz.upperlevel.commandapi.commands.NodeCommand;
import xyz.upperlevel.verifier.exercises.Exercise;
import xyz.upperlevel.verifier.exercises.def.MultipleChoiceExercise;
import xyz.upperlevel.verifier.exercises.def.MultipleChoiceExerciseHandler;
import xyz.upperlevel.verifier.server.Main;
import xyz.upperlevel.verifier.server.assignments.AssignmentManager;
import xyz.upperlevel.verifier.server.login.AuthData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class AssignmentCommand extends NodeCommand {
    public AssignmentCommand() {
        super("assignment");
        addAlias("ass");

        registerSub(new LoadCommand());
        registerSub(new UnloadCommand());
        registerSub(new TestCommand());
        registerSub(new UnCommittedCommand());
        registerSub(new CommittedCommand());
    }

    public static class LoadCommand extends Command {
        public LoadCommand() {
            super("load");
        }

        @CommandRunner
        public void run(String path) {
            File file = new File(path);
            if(!file.exists()) {
                System.err.println("The path doesn't exist");
                return;
            }
            if(!file.isFile()) {
                System.err.println("The path passed isn't a file");
                return;
            }
            AssignmentManager manager = Main.getAssignmentManager();
            if(manager.getCurrent() != null) {
                System.err.println("There is already a hosted assignment, write \"unload\" to unmount it");
                return;
            }
            try {
                manager.load(file);
            } catch (Exception e) {
                System.err.println("Error while loading file:");
                e.printStackTrace();
                return;
            }
            System.out.println("Assignment loaded successfully");
        }
    }

    public static class UnloadCommand extends Command {
        public UnloadCommand() {
            super("unload");
        }

        @CommandRunner
        public void run() {
            AssignmentManager manager = Main.getAssignmentManager();

            if(manager.getCurrent() == null) {
                System.out.println("No assignment loaded");
            } else {
                manager.terminate();
                System.out.println("Assignment successfully unloaded");
            }
        }
    }

    public static class TestCommand extends Command {
        public TestCommand() {
            super("test");
        }

        @CommandRunner
        public void run(String path) {
            File file = new File(path);
            if(file.exists()) {
                System.err.println("The path already exist");
                return;
            }
            xyz.upperlevel.verifier.server.assignments.Assignment ass = new xyz.upperlevel.verifier.server.assignments.Assignment(Arrays.asList(
                    exe(true, "To be or not to be?", "To be", "Not to be"),
                    exe(false, "Do you want ted-learning?", "No", "Mayb...No", "ehhhhh No", "Uhmmmm...No", "Lol, no!")
            ), "test-id");

            try(FileWriter writer = new FileWriter(file)) {
                new Yaml().dump(ass.toYamlRequest(), writer);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            System.out.println("Test assignment wrote successfully");
        }


        private static Exercise<?> exe(boolean multi, String question, String... choices) {
            MultipleChoiceExercise exercise = new MultipleChoiceExercise(new MultipleChoiceExerciseHandler());
            exercise.multiple = multi;
            exercise.question = question;
            exercise.choices = Arrays.asList(choices);
            return exercise;
        }
    }

    public static class CommittedCommand extends Command {

        public CommittedCommand() {
            super("committed");
            addAlias("comm");
        }

        @CommandRunner
        public void run() {
            System.out.println("Checking users");
            AssignmentManager manager = Main.getAssignmentManager();
            getUserThat(
                    manager::hasCurrentAssignment,
                    d -> System.out.println("-" + d.getClazz() + "->" + d.getUsername())
            );
        }
    }

    public static class UnCommittedCommand extends Command {

        public UnCommittedCommand() {
            super("uncommitted");
            addAlias("!committed");
            addAlias("noncommitted");
            addAlias("noncommit");
            addAlias("noncomm");
        }

        @CommandRunner
        public void run() {
            System.out.println("Checking users");
            AssignmentManager manager = Main.getAssignmentManager();
            getUserThat(
                    d -> !manager.hasCurrentAssignment(d),
                    d -> System.out.println("-" + d.getClazz() + "->" + d.getUsername())
            );
        }
    }



    public static void getUserThat(Predicate<AuthData> pred, Consumer<AuthData> use) {
        List<AuthData> users = Main.getLoginManager().getUsers();
        users.stream()
                .filter(pred)
                .sorted(AuthData::compare)
                .forEach(use);
    }
}

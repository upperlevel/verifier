package xyz.upperlevel.verifier.server.ui.console.commands;

import xyz.upperlevel.commandapi.CommandRunner;
import xyz.upperlevel.commandapi.commands.Command;
import xyz.upperlevel.commandapi.commands.NodeCommand;
import xyz.upperlevel.verifier.exercises.ExerciseType;
import xyz.upperlevel.verifier.exercises.ExerciseTypeManager;
import xyz.upperlevel.verifier.server.Main;

public class ExerciseCommand extends NodeCommand{
    public ExerciseCommand() {
        super("exercises");
        addAlias("exercise");
        addAlias("exe");

    }

    public static class ListCommand extends Command {

        public ListCommand() {
            super("list");
        }

        @CommandRunner
        public void run() {
            ExerciseTypeManager manager = Main.getExerciseTypeManager();
            for(ExerciseType<?, ?> exe : manager.get())
                System.out.println("-" + exe.type);
        }
    }
}

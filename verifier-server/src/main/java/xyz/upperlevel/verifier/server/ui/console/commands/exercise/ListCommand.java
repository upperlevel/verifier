package xyz.upperlevel.verifier.server.ui.console.commands.exercise;

import xyz.upperlevel.commandapi.commands.Command;
import xyz.upperlevel.commandapi.executor.CommandRunner;
import xyz.upperlevel.verifier.exercises.ExerciseType;
import xyz.upperlevel.verifier.exercises.ExerciseTypeManager;
import xyz.upperlevel.verifier.server.Main;

public class ListCommand extends Command {
    public ListCommand() {
        super("list", "lists the loaded exercise types");
    }

    @CommandRunner
    public void run() {
        ExerciseTypeManager manager = Main.getExerciseTypeManager();
        for(ExerciseType<?, ?> exe : manager.get())
            System.out.println("-" + exe.type);
    }
}

package xyz.upperlevel.verifier.server.ui.console.commands;

import xyz.upperlevel.commandapi.commands.NodeCommand;

public class ExerciseCommand extends NodeCommand{
    public ExerciseCommand() {
        super("exercise");
        addAlias("exe");

        registerPackageRelative("exercise");
    }
}

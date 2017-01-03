package xyz.upperlevel.verifier.server.ui.console.commands;

import xyz.upperlevel.commandapi.commands.NodeCommand;

public class AssignmentCommand extends NodeCommand {
    public AssignmentCommand() {
        super("assignment");
        addAlias("ass");

        registerPackageRelative("assignment");
    }
}

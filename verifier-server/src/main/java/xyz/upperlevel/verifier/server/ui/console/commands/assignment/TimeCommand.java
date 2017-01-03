package xyz.upperlevel.verifier.server.ui.console.commands.assignment;

import xyz.upperlevel.commandapi.commands.NodeCommand;

public class TimeCommand extends NodeCommand {
    public TimeCommand() {
        super("time", "commands assignment's end time");
        registerPackageRelative("time");
    }
}

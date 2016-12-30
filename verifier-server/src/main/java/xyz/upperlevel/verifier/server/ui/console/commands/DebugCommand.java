package xyz.upperlevel.verifier.server.ui.console.commands;

import xyz.upperlevel.commandapi.commands.NodeCommand;

public class DebugCommand extends NodeCommand {
    public DebugCommand() {
        super("debug");
        registerPackageRelative("debug");
    }
}

package xyz.upperlevel.verifier.server.ui.console.commands;

import xyz.upperlevel.commandapi.commands.NodeCommand;

public class LoginCommand extends NodeCommand {
    public LoginCommand() {
        super("login", "all the user-managment commands");

        registerPackageRelative("login");
    }
}

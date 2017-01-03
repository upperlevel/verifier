package xyz.upperlevel.verifier.server.ui.console.commands.login;

import xyz.upperlevel.commandapi.commands.Command;
import xyz.upperlevel.commandapi.executor.CommandRunner;
import xyz.upperlevel.verifier.server.Main;
import xyz.upperlevel.verifier.server.login.LoginManager;

public class ReloadCommand extends Command {
    public ReloadCommand() {
        super("reload", "reloads all the users from file");
    }

    @CommandRunner
    public void run() {
        LoginManager manager = Main.getLoginManager();
        manager.reload();
    }
}

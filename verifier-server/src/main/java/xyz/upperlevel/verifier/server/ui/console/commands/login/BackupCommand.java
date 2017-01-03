package xyz.upperlevel.verifier.server.ui.console.commands.login;

import xyz.upperlevel.commandapi.commands.Command;
import xyz.upperlevel.commandapi.executor.CommandRunner;
import xyz.upperlevel.verifier.server.Main;

import java.io.IOException;

public class BackupCommand extends Command {
    public BackupCommand() {
        super("backup", "creates a copy of the user directory into the backup folder");
    }

    @CommandRunner
    public void run() {
        try {
            Main.getLoginManager().backupFiles();
        } catch (IOException e) {
            System.err.println("Cannot create backup entry!");
            e.printStackTrace();
        }
    }
}

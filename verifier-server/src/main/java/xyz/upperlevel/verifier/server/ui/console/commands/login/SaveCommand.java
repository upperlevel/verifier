package xyz.upperlevel.verifier.server.ui.console.commands.login;

import xyz.upperlevel.commandapi.commands.Command;
import xyz.upperlevel.commandapi.executor.CommandRunner;
import xyz.upperlevel.verifier.server.Main;

import java.io.IOException;

public class SaveCommand extends Command {
    public SaveCommand() {
        super("save", "saves the local changes in the files");
    }

    @CommandRunner
    public void run() {
        try {
            Main.getLoginManager().saveToFiles();
        } catch (IOException e) {
            System.err.println("Cannot save changes!");
            e.printStackTrace();
        }
    }
}
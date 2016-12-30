package xyz.upperlevel.verifier.server.ui.console.commands;

import xyz.upperlevel.commandapi.CommandRunner;
import xyz.upperlevel.commandapi.commands.Command;
import xyz.upperlevel.verifier.server.Main;
import xyz.upperlevel.verifier.server.ui.console.ConsoleUI;

public class StopCommand extends Command {
    public StopCommand() {
        super("stop");
    }

    @CommandRunner
    public void run() {
        ConsoleUI.running = false;
        System.out.println("Closing server...");
        try {
            Main.getServer().shutdown();
        } catch (Exception e) {
            System.err.println("Exception caught when shutdowning the sercer");
            e.printStackTrace();
        }
    }
}

package xyz.upperlevel.verifier.server.ui.console.commands.assignment;

import xyz.upperlevel.commandapi.commands.Command;
import xyz.upperlevel.commandapi.executor.CommandRunner;
import xyz.upperlevel.verifier.server.Main;
import xyz.upperlevel.verifier.server.assignments.AssignmentManager;

public class UnloadCommand extends Command {
    public UnloadCommand() {
        super("unload", "unloads the loaded assignment");
    }

    @CommandRunner
    public void run() {
        AssignmentManager manager = Main.getAssignmentManager();

        if(manager.getCurrent() == null) {
            System.out.println("No assignment loaded");
        } else {
            manager.terminate();
            System.out.println("Assignment successfully unloaded");
        }
    }
}

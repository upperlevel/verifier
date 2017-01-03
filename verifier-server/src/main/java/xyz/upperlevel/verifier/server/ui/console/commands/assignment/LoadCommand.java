package xyz.upperlevel.verifier.server.ui.console.commands.assignment;

import xyz.upperlevel.commandapi.commands.Command;
import xyz.upperlevel.commandapi.executor.CommandRunner;
import xyz.upperlevel.commandapi.executor.ParamName;
import xyz.upperlevel.verifier.server.Main;
import xyz.upperlevel.verifier.server.assignments.AssignmentManager;

import java.io.File;

public class LoadCommand extends Command {
    public LoadCommand() {
        super("load", "loads an assignment");
    }

    @CommandRunner
    public void run(@ParamName("path") String path) {
        File file = new File(path);
        if(!file.exists()) {
            System.err.println("The path doesn't exist");
            return;
        }
        if(!file.isFile()) {
            System.err.println("The path passed isn't a file");
            return;
        }
        AssignmentManager manager = Main.getAssignmentManager();
        if(manager.getCurrent() != null) {
            System.err.println("There is already a hosted assignment, write \"unload\" to unmount it");
            return;
        }
        try {
            manager.load(file);
        } catch (Exception e) {
            System.err.println("Error while loading file:");
            e.printStackTrace();
            return;
        }
        System.out.println("Assignment loaded successfully");
    }
}

package xyz.upperlevel.verifier.server.ui.console.commands.assignment;

import xyz.upperlevel.commandapi.commands.Command;
import xyz.upperlevel.commandapi.executor.CommandRunner;
import xyz.upperlevel.verifier.server.Main;
import xyz.upperlevel.verifier.server.assignments.AssignmentManager;

import static xyz.upperlevel.verifier.server.ui.console.commands.assignment.CommittedCommand.getUserThat;

public class UnCommittedCommand extends Command {
    public UnCommittedCommand() {
        super("uncommitted", "displays a list of the users that do not have committed yet");
        addAlias("!committed");
        addAlias("noncommitted");
        addAlias("noncommit");
        addAlias("noncomm");
    }

    @CommandRunner
    public void run() {
        System.out.println("Checking users");
        AssignmentManager manager = Main.getAssignmentManager();
        getUserThat(
                d -> !manager.hasCurrentAssignment(d),
                d -> System.out.println("-" + d.getClazz() + "->" + d.getUsername())
        );
    }
}

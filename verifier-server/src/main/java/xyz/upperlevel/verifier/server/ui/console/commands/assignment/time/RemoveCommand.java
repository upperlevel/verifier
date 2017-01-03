package xyz.upperlevel.verifier.server.ui.console.commands.assignment.time;

import xyz.upperlevel.commandapi.commands.Command;
import xyz.upperlevel.commandapi.executor.CommandRunner;
import xyz.upperlevel.verifier.server.assignments.TimeSyncUtil;

public class RemoveCommand extends Command {
    public RemoveCommand() {
        super("remove", "remove a endTime from the assignment");
        addAlias("unset");
    }

    @CommandRunner
    public void run() {
        TimeSyncUtil.setTime(null);
    }
}

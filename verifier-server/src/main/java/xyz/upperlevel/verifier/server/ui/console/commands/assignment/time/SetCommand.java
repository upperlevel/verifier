package xyz.upperlevel.verifier.server.ui.console.commands.assignment.time;

import xyz.upperlevel.commandapi.commands.Command;
import xyz.upperlevel.commandapi.executor.CommandRunner;
import xyz.upperlevel.commandapi.executor.ParamName;
import xyz.upperlevel.verifier.server.Main;
import xyz.upperlevel.verifier.server.assignments.TimeSyncUtil;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class SetCommand extends Command {
    public SetCommand() {
        super("set", "Sets the remaining time");
    }

    @CommandRunner
    public void run(@ParamName("time") String time) {
        if(Main.currentAssignment() == null) {
            System.out.println("No assignment loaded!");
            return;
        }
        LocalTime lt;

        try {
            lt = TimeSettings.formatter.parse(time, LocalTime::from);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid parameter time, doesn't match pattern \"" + TimeSettings.pattern + "\"");
            return;
        }

        TimeSyncUtil.setTime(lt);

        System.out.println("Time set to " + TimeSettings.formatter.format(lt));
        System.out.println("Remaining: " + TimeSettings.formatDuration(Duration.between(LocalTime.now(), lt)));
    }
}

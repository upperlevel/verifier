package xyz.upperlevel.verifier.server.ui.console.commands.assignment.time;

import xyz.upperlevel.commandapi.commands.Command;
import xyz.upperlevel.commandapi.executor.CommandRunner;
import xyz.upperlevel.commandapi.executor.ParamName;

import java.time.format.DateTimeFormatter;

public class PatternCommand extends Command {
    public PatternCommand() {
        super("pattern", "command to get/set the input/output pattern of time");
    }

    @CommandRunner
    public void run() {
        System.out.println("Current: " + TimeSettings.pattern);
    }

    @CommandRunner
    public void run(@ParamName("new_pattern") String np) {
        DateTimeFormatter formatter;
        try {
            formatter = DateTimeFormatter.ofPattern(np);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid pattern");
            e.printStackTrace();
            return;
        }

        TimeSettings.formatter = formatter;
        TimeSettings.pattern = np;

        System.out.println("Pattern set!");
    }
}

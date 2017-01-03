package xyz.upperlevel.verifier.server.ui.console.commands.assignment.time;

import xyz.upperlevel.commandapi.commands.Command;
import xyz.upperlevel.commandapi.executor.CommandRunner;
import xyz.upperlevel.verifier.server.Main;
import xyz.upperlevel.verifier.server.assignments.TimeSyncUtil;

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.locks.StampedLock;

public class GetCommand extends Command {
    public GetCommand() {
        super("get", "gets the time currently set");
    }

    @CommandRunner
    public void run() {
        LocalTime endTime;

        {
            StampedLock lock = TimeSyncUtil.lock;
            long stamp = lock.readLock();
            try {
                endTime = Main.currentAssignment().getEndTime();
            } finally {
                lock.unlockRead(stamp);
            }
        }

        if(endTime != null) {
            Duration delta = Duration.between(LocalTime.now(), endTime);

            System.out.println("End time: " + TimeSettings.formatter.format(endTime));
            System.out.println("Remaining: " + TimeSettings.formatDuration(delta));
        } else
            System.out.println("No time set");
    }
}

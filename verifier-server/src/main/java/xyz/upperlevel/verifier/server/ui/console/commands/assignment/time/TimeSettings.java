package xyz.upperlevel.verifier.server.ui.console.commands.assignment.time;

import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class TimeSettings {
    public static String pattern = "HH:mm:ss";
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

    public static String formatDuration(Duration duration) {
        String res = "";
        long hours = duration.toHours(), minutes = duration.toMinutes(), secs = duration.getSeconds();

        if(secs < 0)
            return "Time Expired";

        if(hours > 0)
            res += hours+  "d ";

        if(minutes > 0)
            res += (minutes % 60) + "m ";

        if(secs > 0)
            res += (secs % 60) + "s";

        return res;
    }
}

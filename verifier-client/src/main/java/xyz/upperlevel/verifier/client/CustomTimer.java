package xyz.upperlevel.verifier.client;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class CustomTimer {
    private Timer timer = null;
    private final Runnable run;

    public CustomTimer(Runnable run) {
        this.run = run;
    }

    public void start(long pause, TimeUnit unit) {
        timer = new Timer("Verifier timer");
        pause = unit.toMillis(pause);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                run.run();
            }
        }, pause, pause);
    }

    public void stop() {
        if(timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
            System.out.println("Timer closed");
        }
    }

    public boolean isRunning() {
        return timer != null;
    }
}

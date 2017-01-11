package xyz.upperlevel.verifier.server.assignments;

import com.google.protobuf.Timestamp;
import lombok.Getter;
import xyz.upperlevel.verifier.proto.protobuf.TimePacket;
import xyz.upperlevel.verifier.server.ClientHandler;
import xyz.upperlevel.verifier.server.Main;
import xyz.upperlevel.verifier.server.login.AuthData;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.StampedLock;

import static xyz.upperlevel.verifier.server.Main.handlers;

public class TimeSyncUtil {

    public static final StampedLock lock = new StampedLock();
    @Getter
    private static TimePacket.Time packet = null;

    public static Set<AuthData> req = ConcurrentHashMap.newKeySet();

    public static Queue<Runnable> timeExpireListeners = new ConcurrentLinkedQueue<>();

    private static Timer timer = null;

    public static void setTime(Timestamp date) {
        final TimePacket.Time packet = TimePacket.Time.newBuilder()
                .setOperation(TimePacket.Operation.SET)
                .setTime(date)
                .build();


        long stamp = lock.writeLock();
        try {
            TimeSyncUtil.packet = packet;

            AssignmentRequest ass = Main.currentAssignment();

            if(ass == null)
                return;

            ass.setTimeUnsafe(date);
            for(ClientHandler handler : handlers().getAll())
                if(handler.getSent() != null)
                handler.getChannel().writeAndFlush(packet);

            req.clear();

            if(timer != null)
                timer.cancel();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    timeExpireListeners.forEach(Runnable::run);
                    timeExpireListeners.clear();
                    timeExpireListeners.add(() -> System.out.println("---->TIME EXPIRED!<----"));
                }
            }, Date.from(date.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant()));

        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public static int requireTime(AuthData data) {
        long stamp = lock.readLock();
        try {
            req.add(data);
            return req.size();
        } finally {
            lock.unlockRead(stamp);
        }
    }
}

package xyz.upperlevel.verifier.server;

import io.netty.channel.Channel;
import lombok.Getter;
import xyz.upperlevel.verifier.exercises.ExerciseType;
import xyz.upperlevel.verifier.proto.*;
import xyz.upperlevel.verifier.server.assignments.AssignmentRequest;
import xyz.upperlevel.verifier.server.assignments.AssignmentResponse;
import xyz.upperlevel.verifier.server.assignments.TimeSyncUtil;
import xyz.upperlevel.verifier.server.assignments.exceptions.AlreadyCommittedException;
import xyz.upperlevel.verifier.server.login.AuthData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.locks.StampedLock;

public class ClientHandler {
    @Getter
    private final Channel channel;

    public AuthData data = null;
    public StampedLock authLock = new StampedLock();

    @Getter
    private AssignmentRequest sent = null;

    public ClientHandler(Channel channel) {
        this.channel = channel;
    }

    public void onConnect() {}


    public void onDisconnect() {
        long stamp = authLock.readLock();
        try {
            if (data != null)
                data.setLogged(null);
        } finally {
            authLock.unlockRead(stamp);
        }
    }

    public void onAssignment(AssignmentPacket packet) {
        log("Received assignment packet");
        if(!checkLogged()) {
            log("[WARN] assignment before login");
            return;
        }
        if(sent != null && !packet.getId().equals(sent.getId())) {
            log("[WARN] bad assignment");
            send(new ErrorPacket(ErrorType.ASSIGNMENT, "Bad assignment, sent:\"" + packet.getId() + "\" expected: \"" + sent.getId() + "\""));
            return;
        }
        try {
            log("Commited assignment");
            long stamp = authLock.readLock();
            try {
                AssignmentRequest curr = Main.currentAssignment();
                if(sent == null || !sent.getId().equals(packet.getId())) {
                    send(new ErrorPacket(ErrorType.ASSIGNMENT, "Bad assignment type!"));
                } else {
                    Main.getAssignmentManager().commit(data, new AssignmentResponse(packet, sent, new Random(data.toSeed())));
                }
            } finally {
                authLock.unlockRead(stamp);
            }
        } catch (AlreadyCommittedException e) {
            log("[WARN] tried to commit an already-commited assignment");
            send(new ErrorPacket(ErrorType.ASSIGNMENT, "Assignment already committed!"));
        }
    }

    public void onError(ErrorPacket packet) {
        Main.getUi().error(packet.getType(), packet.getMessage());
    }

    public void onExeRequest(ExerciseTypePacket packet) {
        log("Received exercise request");
        if(!checkLogged()) {
            log("[WARN] exercise request before login");
            return;
        }
        ExerciseType type = Main.getExerciseTypeManager().get(packet.getName());
        if(type == null) {
            log("[WARN]Type not found: \"" + packet.getName() + "\"");
            channel.writeAndFlush(new ErrorPacket(ErrorType.TEST_TYPE, "Type not found \"" + packet.getName() + "\""));
            return;
        }
        Path file = Main.getExerciseTypeManager().getFile(type);
        if(file == null)
            return;
        try {
            send(new ExerciseTypePacket(packet.getName(), Files.readAllBytes(file)));
        } catch (IOException e) {
            System.err.println("[WARNING]Error finding jar of \"" + packet.getName() + "\"!");
            e.printStackTrace();
        }
    }

    public void onLogin(LoginPacket packet) {
        AuthData data = Main.getLoginManager().get(packet.getClazz(), new HashSet<>(Arrays.asList(packet.getUser().toLowerCase().split(" "))));
        if (data == null) {
            log("Login error: bad username");
            send(new ErrorPacket(ErrorType.LOGIN_BAD_USER, "Username not found"));
        } else if (Arrays.equals(packet.getPassword(), data.getPassword())) {
            long stamp = authLock.writeLock();
            try {
                this.data = data;
                log("Logged in");
                this.data.setLogged(this);
            } finally {
                authLock.unlockWrite(stamp);
            }

            sendAssignment();
        } else {
            log("Login error: bad password");
            send(new ErrorPacket(ErrorType.LOGIN_BAD_PASSWORD, "Wrong password"));
        }
    }

    public void sendTime() {
        StampedLock lock = TimeSyncUtil.lock;
        long stamp = lock.readLock();
        try {
            TimePacket packet = TimeSyncUtil.getPacket();
            if(packet == null)
                return;
            send(packet);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public void onTime(TimePacket packet) {
        if(packet.getType() == TimePacket.PacketType.REQUEST) {
            log("Request time! -> " + TimeSyncUtil.requireTime(data));
        } else if(packet.getType() == TimePacket.PacketType.REQUEST) {
            log("Packet error: client sent a SET time packet!");
            send(new ErrorPacket(ErrorType.BAD_PROTOCOL, "The client can't send a SET time packet"));
        } else {
            log("Packet error: Time packet not implemented yet: " + packet.getType().name());
            send(new ErrorPacket(ErrorType.BAD_PROTOCOL, "Time packet " + packet.getType().name() + " not implemented!"));
        }
    }

    public void sendAssignment() {
        sent = Main.currentAssignment();
        if(sent != null) {
            send(sent.getPacket(new Random(data.toSeed())));
            sendTime();
        } else
            Main.getAssignmentManager().addListener(ass ->  {
                send((sent = ass).getPacket(new Random(data.toSeed())));
                sendTime();
            });
    }

    private boolean checkLogged() {
        if (data == null) {
            badProtocolStateError();
            return false;
        } else return true;
    }

    private void badProtocolStateError() {
        send(new ErrorPacket(ErrorType.NOT_LOGGED_ID, "You can do this operation only after login"));
    }

    private void log(String str) {
        System.out.println(str);
    }

    private void send(Object o) {
        channel.writeAndFlush(o);
    }
}

package xyz.upperlevel.verifier.server;

import io.netty.channel.Channel;
import lombok.Getter;
import xyz.upperlevel.verifier.proto.protobuf.AssignmentPacket;
import xyz.upperlevel.verifier.proto.protobuf.ErrorPacket;
import xyz.upperlevel.verifier.proto.protobuf.ErrorPacket.ErrorType;
import xyz.upperlevel.verifier.proto.protobuf.LoginPacket;
import xyz.upperlevel.verifier.proto.protobuf.TimePacket;
import xyz.upperlevel.verifier.server.assignments.AssignmentRequest;
import xyz.upperlevel.verifier.server.assignments.AssignmentResponse;
import xyz.upperlevel.verifier.server.assignments.TimeSyncUtil;
import xyz.upperlevel.verifier.server.assignments.exceptions.AlreadyCommittedException;
import xyz.upperlevel.verifier.server.login.AuthData;

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

    public void onAssignment(AssignmentPacket.Assignment packet) {
        log("Received assignment packet");
        if(!checkLogged()) {
            log("[WARN] assignment before login");
            return;
        }
        if(sent != null && !packet.getId().equals(sent.getId())) {
            log("[WARN] bad assignment");
            error(ErrorType.ASSIGNMENT, "Bad assignment, sent:\"" + packet.getId() + "\" expected: \"" + sent.getId() + "\"");
            return;
        }
        try {
            log("Commited assignment");
            long stamp = authLock.readLock();
            try {
                AssignmentRequest curr = Main.currentAssignment();
                if(sent == null || !sent.getId().equals(packet.getId())) {
                    error(ErrorType.ASSIGNMENT, "Bad assignment type!");
                } else {
                    Main.getAssignmentManager().commit(data, new AssignmentResponse(packet, sent, new Random(data.toSeed())));
                }
            } finally {
                authLock.unlockRead(stamp);
            }
        } catch (AlreadyCommittedException e) {
            log("[WARN] tried to commit an already-commited assignment");
            error(ErrorType.ASSIGNMENT, "Assignment already committed!");
        }
    }

    private void error(ErrorType errorType, String msg) {
        send(
                ErrorPacket.Error.newBuilder()
                        .setType(errorType)
                        .setMessage(msg)
                        .build()
        );
    }

    public void onError(ErrorPacket.Error packet) {
        Main.getUi().error(packet.getType(), packet.getMessage());
    }

    public void onLogin(LoginPacket.Login packet) {
        AuthData data = Main.getLoginManager().get(packet.getClazz(), new HashSet<>(Arrays.asList(packet.getUser().toLowerCase().split(" "))));
        if (data == null) {
            log("Login error: bad username");
            error(ErrorType.LOGIN, "Username not found");
        } else if (data.getPassword().equals(packet.getPassword())) {
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
            error(ErrorType.LOGIN, "Wrong password");
        }
    }

    public void sendTime() {
        StampedLock lock = TimeSyncUtil.lock;
        long stamp = lock.readLock();
        try {
            TimePacket.Time packet = TimeSyncUtil.getPacket();
            if(packet == null)
                return;
            send(packet);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public void onTime(TimePacket.Time packet) {
        if(packet.getOperation() == TimePacket.Operation.GET) {
            log("Request time! -> " + TimeSyncUtil.requireTime(data));
        } else if(packet.getOperation() == TimePacket.Operation.SET) {
            log("Packet error: client sent a SET time packet!");
            error(ErrorType.BAD_PROTOCOL, "The client can't send a SET time packet");
        } else {
            log("Packet error: Time packet not implemented yet: " + packet.getOperation().name());
            error(ErrorType.BAD_PROTOCOL, "Time packet " + packet.getOperation().name() + " not implemented!");
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
        error(ErrorType.NOT_LOGGED_ID, "You can do this operation only after login");
    }

    private void log(String str) {
        System.out.println(str);
    }

    private void send(Object o) {
        channel.writeAndFlush(o);
    }
}

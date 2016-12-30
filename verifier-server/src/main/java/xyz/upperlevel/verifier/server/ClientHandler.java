package xyz.upperlevel.verifier.server;

import io.netty.channel.Channel;
import lombok.Getter;
import xyz.upperlevel.verifier.exercises.ExerciseType;
import xyz.upperlevel.verifier.proto.*;
import xyz.upperlevel.verifier.server.assignments.Assignment;
import xyz.upperlevel.verifier.server.assignments.exceptions.AlreadyCommittedException;
import xyz.upperlevel.verifier.server.login.AuthData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class ClientHandler {
    @Getter
    private final Channel channel;

    private AuthData data = null;

    private Assignment sent = null;

    public ClientHandler(Channel channel) {
        this.channel = channel;
    }

    public void onConnect() {}


    public void onDisconnect() {
        if(data != null)
            data.setLogged(false);
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
            Main.getAssignmentManager().commit(data, new Assignment(packet));
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
        AuthData data = Main.getLoginManager().get(packet.getClazz(), packet.getUser().toLowerCase());
        if (data == null) {
            log("Login error: bad username");
            send(new ErrorPacket(ErrorType.LOGIN_BAD_USER, "Username not found"));
        } else if (Arrays.equals(packet.getPassword(), data.getPassword())) {
            this.data = data;
            log("Logged in");
            data.setLogged(true);

            sendAssignment();
        } else {
            log("Login error: bad password");
            send(new ErrorPacket(ErrorType.LOGIN_BAD_PASSWORD, "Wrong password"));
        }
    }

    public void sendAssignment() {
        Assignment current = Main.currentAssignment();
        if(current != null)
            send(Main.currentAssignment().getPacket());
        else
            Main.getAssignmentManager().addListener(ass -> send(ass.getPacket()));
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

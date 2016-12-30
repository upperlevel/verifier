package xyz.upperlevel.verifier.server;

import lombok.Getter;
import xyz.upperlevel.verifier.exercises.ExerciseTypeManager;
import xyz.upperlevel.verifier.packetlib.simple.PacketExecutorManager;
import xyz.upperlevel.verifier.packetlib.simple.SimpleServer;
import xyz.upperlevel.verifier.proto.*;
import xyz.upperlevel.verifier.server.assignments.Assignment;
import xyz.upperlevel.verifier.server.assignments.AssignmentManager;
import xyz.upperlevel.verifier.server.login.LoginManager;
import xyz.upperlevel.verifier.server.ui.UI;
import xyz.upperlevel.verifier.server.ui.console.ConsoleUI;

public class Main {
    @Getter
    public static final UI ui = new ConsoleUI();
    @Getter
    private static SimpleServer server;
    @Getter
    private static Thread serverThread;
    @Getter
    private static Thread uiThread;


    @Getter
    private static LoginManager loginManager = new LoginManager();
    @Getter
    private static AssignmentManager assignmentManager = new AssignmentManager();
    @Getter
    private static ExerciseTypeManager exerciseTypeManager = new ExerciseTypeManager();



    private static ConnListener listener = new ConnListener();

    private static final ClientManager handlers = new ClientManager();


    public static void main(String... args) {
        ui.init(args);

        loginManager.registerFromFiles();
        ui.askConnInfo((port, options) -> {
            server = new SimpleServer(port, options);
            ProtocolUtils.registerDefPackets(server.getPacketManager());
            initExecutors();
            serverThread = new Thread(() -> {
                try {
                    System.out.println("Starting server");
                    server.start();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Closing server");
            });
            serverThread.start();
            uiThread = Thread.currentThread();
            System.out.println("UI started");
            ui.start();
            System.out.println("Exiting ui thread");
        });
    }

    public static Assignment currentAssignment() {
        return assignmentManager.getCurrent();
    }

    private static void initExecutors() {
        ProtocolUtils.registerDefPackets(server.getPacketManager());
        PacketExecutorManager exe = server.getExecutorManager();
        exe.registerChannelConnect(listener::onConnect);
        exe.registerChannelDisconnect(listener::onDisconnect);
        exe.register(AssignmentPacket.class, listener::onAssignment);
        exe.register(ErrorPacket.class, listener::onError);
        exe.register(ExerciseTypePacket.class, listener::onExeRequest);
        exe.register(LoginPacket.class, listener::onLogin);
    }

    public static ClientManager handlers() {
        return handlers;
    }
}

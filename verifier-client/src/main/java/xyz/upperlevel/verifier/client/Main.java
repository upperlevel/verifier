package xyz.upperlevel.verifier.client;

import javafx.application.Platform;
import xyz.upperlevel.verifier.client.assignments.AssignmentRequest;
import xyz.upperlevel.verifier.client.assignments.AssignmentResponse;
import xyz.upperlevel.verifier.client.conn.Connection;
import xyz.upperlevel.verifier.client.conn.ConnectionHandler;
import xyz.upperlevel.verifier.client.gui.SimpleGUI;
import xyz.upperlevel.verifier.client.gui.UI;
import xyz.upperlevel.verifier.exercises.ExerciseResponse;
import xyz.upperlevel.verifier.exercises.ExerciseType;
import xyz.upperlevel.verifier.exercises.ExerciseTypeManager;
import xyz.upperlevel.verifier.proto.ErrorType;
import xyz.upperlevel.verifier.proto.ExerciseData;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main implements PacketListener{

    private static final Main instance = new Main();

    private final Connection conn = new ConnectionHandler();
    private final UI ui = new SimpleGUI();
    private final ExerciseTypeManager exerciseManager = new ExerciseTypeManager();

    private State state = State.INIT;

    public static void main(String... args) {
        instance.start();
    }

    private void start() {
        System.out.println("Starting listener");
        conn.setListener(this);
        System.out.println("Init windows");
        ui.initWindows(() ->  {
            System.out.println("Init connection");
            initConnection(this::login);
        });
    }

    private void login() {
        state = State.LOGIN_IN;
        System.out.println("Requesting login");
        ui.requestLogin(auth ->{
            if(auth == null) {
                shutdown();
                return;
            }
            System.out.println("Sending login");
            conn.sendLogin(auth.getClazz(), auth.getUsername(), auth.getPassword().toCharArray());
            System.out.println("Login sent, waiting for assignment");
            state = State.ASSIGNMENT_WAIT;
        });
    }

    private void initConnection(Runnable callback) {
        if(state != State.INIT && state != State.CONNECTION_IN)
            throw new IllegalStateException("Phase already passed, cannot go back to ask ip");
        state = State.CONNECTION_IN;
        System.out.println("Requesting address");
        ui.requestAddress(
                (host, port) -> {
                    if(host == null && port == null) {
                        shutdown();
                        return;
                    }
                    try {
                        state = State.CONNECTING;
                        conn.init(host, port);
                        callback.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                        ui.error(e);
                        state = State.CONNECTION_IN;
                        initConnection(callback);
                    }
                }
        );
    }

    @Override
    public void onExerciseType(String exName, byte[] data) {
        try {
            exerciseManager.register(exName, data);
        } catch (IOException | IllegalAccessException | ClassNotFoundException | InstantiationException e) {
            e.printStackTrace();
            ui.error(e);
        }
    }

    @Override
    public void onAsignment(String id, List<ExerciseData> exercises) {
        System.out.println("Received assignment data, parsing");
        parseAssignment(id, exercises, ui::openAssignment);
    }

    private void parseAssignment(String id, List<ExerciseData> exercises, Consumer<AssignmentRequest> callback) {
        state = State.ASSIGNMENT_WAIT;
        ExerciseUtil.getAllOrWait(
                exercises.stream()
                        .map(ExerciseData::getType)
                        .collect(Collectors.toList()),
                (List<ExerciseType<?, ?>> handlers) -> {
                    if (exercises.size() != handlers.size())
                        throw new RuntimeException("The handlers returned aren't paired with the exercises!");
                    AssignmentRequest assignment = new AssignmentRequest(
                            id,
                            IntStream
                                    .range(0, exercises.size())
                                    .mapToObj(i -> handlers.get(i).decodeRequest(exercises.get(i).getData()))
                                    .collect(Collectors.toList())
                    );
                    state = State.ASSIGNMENT_EXECUTING;
                    callback.accept(assignment);
                }
        );
    }

    public static void shutdown() {
        System.out.println("---------SHUTDOWN---------");
        System.out.println("IO threads shutdown....");
        getConnection().shutdown();
        System.out.println("JavaFX process shutdown...");
        Platform.exit();
        System.out.println("Shutdown completed");
    }

    public static void onSendAssignment(AssignmentResponse assignment) {
        instance.conn.sendAssignment(
                assignment.getId(),
                assignment.getExercises().stream()
                        .map(ExerciseResponse::encode)
                        .collect(Collectors.toList())
        );
        shutdown();
    }

    @Override
    public void onError(ErrorType error, String message) {
        ui.error(error, message);
        if(error == ErrorType.LOGIN_BAD_USER || error == ErrorType.LOGIN_BAD_PASSWORD)
            login();
    }

    public static Connection getConnection() {
        return instance.conn;
    }

    public static UI getUI() {
        return instance.ui;
    }

    public static ExerciseTypeManager getExerciseManager() {
        return instance.exerciseManager;
    }

    public enum State {
        INIT, CONNECTION_IN, CONNECTING, LOGIN_IN, ASSIGNMENT_WAIT, ASSIGNMENT_EXECUTING, END
    }
}

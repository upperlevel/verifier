package me.upperlevel.verifier.client;

import me.upperlevel.verifier.client.conn.Connection;
import me.upperlevel.verifier.client.conn.ConnectionHandler;
import me.upperlevel.verifier.client.gui.SimpleGUI;
import me.upperlevel.verifier.client.gui.UI;
import me.upperlevel.verifier.proto.ErrorType;
import me.upperlevel.verifier.proto.ExerciseData;

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main implements PacketListener{
    private static Logger logger = Logger.getLogger("Main");

    private static final Main instance = new Main();

    private final Connection conn = new ConnectionHandler();
    private final UI ui = new SimpleGUI();
    private final ExerciseManager exerciseManager = new ExerciseManager();

    private State state = State.INIT;


    public static void main(String... args) {
        new Main().start();
    }

    private void start() {
        conn.setListener(this);
        ui.initWindows(() -> initConnection(() -> {}));
    }

    private void initConnection(Runnable callback) {
        if(state != State.INIT && state != State.CONNECTION_IN)
            throw new IllegalStateException("Phase already passed, cannot go back to ask ip");
        state = State.CONNECTION_IN;
        ui.requestAddress(
                (host, port) -> {
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
        exerciseManager.register(exName, data);
    }

    @Override
    public void onAsignment(List<ExerciseData> exercises) {
        parseAssignment(exercises, ui::openAssignment);
    }

    private void parseAssignment(List<ExerciseData> exercises, Consumer<Assignment> callback) {
        state = State.ASSIGNMENT_WAIT;
        exerciseManager.getAllOrWait(
                exercises.stream()
                        .map(ExerciseData::getExercise_type)
                        .collect(Collectors.toList()),
                (List<ExerciseHandler<?>> handlers) -> {
                    if (exercises.size() != handlers.size())
                        throw new RuntimeException("The handlers returned arent paired with the exercises!");
                    Assignment assignment = new Assignment(
                            IntStream
                                    .range(0, exercises.size())
                                    .mapToObj(i -> handlers.get(i).decode(exercises.get(i).getExercize_data()))
                                    .collect(Collectors.toList())
                    );
                    state = State.ASSIGNMENT_EXECUTING;
                    callback.accept(assignment);
                }
        );
    }

    public static void onSendAssignment(Assignment assignment) {
        instance.conn.sendAssignment(instance.exerciseManager.ancodeAll(assignment.getExercises()));
    }

    @Override
    public void onError(ErrorType error, String message) {
        ui.error(error, message);
    }

    public static Connection getConnection() {
        return instance.conn;
    }

    public static UI getUI() {
        return instance.ui;
    }

    public static ExerciseManager getExerciseManager() {
        return instance.exerciseManager;
    }

    public enum State {
        INIT, CONNECTION_IN, CONNECTING, ASSIGNMENT_WAIT, ASSIGNMENT_EXECUTING, END
    }
}

package xyz.upperlevel.verifier.client.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import xyz.upperlevel.verifier.client.CustomTimer;
import xyz.upperlevel.verifier.client.Main;
import xyz.upperlevel.verifier.client.assignments.AssignmentRequest;
import xyz.upperlevel.verifier.client.assignments.AssignmentResponse;
import xyz.upperlevel.verifier.exercises.ExerciseRequest;
import xyz.upperlevel.verifier.exercises.ExerciseResponse;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AssignmentGUIController implements Initializable {
    private static final String timeStr = "%1$dh %2$02dm %3$02ds";

    @FXML
    public ListView<ExerciseResponse> exercises_view;

    @FXML
    public Label ex_number;
    @FXML
    public Label ex_max;
    @FXML
    public Label ex_percentage;
    @FXML
    public Label ex_time;

    @FXML
    public AnchorPane ex_container;

    @FXML
    public Label left_status;
    @FXML
    public Label right_status;

    @FXML
    public Button prev_button;
    @FXML
    public Button next_button;

    private int index = 1;

    @FXML
    private ObservableList<ExerciseResponse> exercises;

    private AssignmentRequest request;

    private CustomTimer timer = new CustomTimer(this::updateTimeStr);

    public AssignmentGUIController() {
        Main.closeListener.add(timer::stop);
    }

    public void init(AssignmentRequest request) {
        init(request, FXCollections.observableList(request.getExercises().stream().map(ExerciseRequest::getResponse).collect(Collectors.toList())));
    }

    public void init(AssignmentRequest request, ObservableList<ExerciseResponse> exercises) {
        this.exercises = Objects.requireNonNull(exercises);
        this.request = request;

        exercises_view.setItems(exercises);
        exercises_view.setCellFactory(new CustomCellFactory());

        ex_max.setText(
                "" + exercises.size()
        );
        updateIndex();

        left_status.setText("");
        right_status.setText("");

        request.timeListener = ((newValue) -> {
            System.out.println("time changed: " + newValue);
            updateTimer(newValue);
        });
        updateTimer(request.getEndTime());
        System.out.println("Set time listener");
    }

    @FXML
    public void onListClick(MouseEvent event) {
        updateIndex(exercises_view.getSelectionModel().getSelectedIndex());
    }

    @FXML
    public void onSend() {
        String[] options = {"Revise", "Send"};
        int response = JOptionPane.showOptionDialog(
                null,
                "Do you want to revise the answers?",
                "Are you sure?",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if(response == 1) {
            AssignmentGUI.getInstance().getStage().hide();
            Main.onSendAssignment(new AssignmentResponse(request, exercises));
            //Main should close itself
        }
    }

    @FXML
    public void onOptions() {
        throw new NotImplementedException();
    }

    @FXML
    public void onClose() {
        if(JOptionPane.showConfirmDialog(
                null,
                "If you close the exercise you will lose every answer\n and you may not be able to retake the test",
                "Are you sure?",
                JOptionPane.OK_CANCEL_OPTION
        ) == JOptionPane.OK_OPTION) {
            close0();
        }
    }

    protected void close0() {
        AssignmentGUI.getInstance().getStage().hide();
        if(timer != null)
            timer.stop();
        Main.shutdown();
    }

    @FXML
    public void onRequestTime() {
        Main.requestTime();
    }

    @FXML
    public void onRequestInfo() {
        throw new NotImplementedException();
    }

    @FXML
    public void onAskHelp() {
        throw new NotImplementedException();
    }

    @FXML
    public void onAbout() {
        throw new NotImplementedException();
    }

    @FXML
    public void onBugReport() {
        throw new NotImplementedException();
    }


    @FXML
    public void onPrevious() {
        updateIndex(index - 1);
    }

    @FXML
    public void onNext() {
        updateIndex(index + 1);
    }

    @FXML
    public void onTestInfo() {
        throw new NotImplementedException();
    }

    private void updateIndex(int newIndex) {
        newIndex = newIndex < 1 ? 1 : newIndex > exercises.size() ? exercises.size() : newIndex;
        if(newIndex != index) {
            index = newIndex;
            updateIndex();
        }
    }

    public void updateIndex() {
        ex_number.setText("" + index);
        ex_percentage.setText(((index*100)/exercises.size()) + "");
        if(index == 1)
            prev_button.setDisable(true);
        else
            prev_button.setDisable(false);

        if(index == exercises.size())
            next_button.setDisable(true);
        else
            next_button.setDisable(false);

        {
            Parent p = exercises.get(index - 1).getGraphics();
            ex_container.getChildren().setAll(p);
            ex_container.setMinHeight(p.prefHeight(-1));
        }
    }

    public void updateTimeStr() {
        final ColoredText time = getTimeStr();
        Platform.runLater(() -> {
            ex_time.setText(time.text);
            ex_time.setTextFill(time.color);
        });
    }

    public void updateTimer(LocalTime newValue) {
        boolean hasTime = newValue != null;

        if(hasTime && !timer.isRunning()) {
            timer.start(1, TimeUnit.SECONDS);
            updateTimeStr();
        } else if(!hasTime && timer.isRunning()) {
            timer.stop();
            updateTimeStr();
        }
    }

    private static final Color TIME_SAFE = Color.web("#3333ff");
    private static final Color TIME_WARNING = Color.web("ff6600");
    private static final Color TIME_EXPIRED = Color.web("#ff0000");

    public ColoredText getTimeStr(){
        LocalTime endTime = request.getEndTime();
        if(endTime !=  null) {
            Duration duration = Duration.between(LocalTime.now(), endTime);

            String str;
            Paint color = null;

            if(duration.isNegative()) {
                duration = duration.abs();
                str = "-";
                color = TIME_EXPIRED;
            } else str = "";

            final long hours = duration.toHours(),
                    minutes = duration.toMinutes() % 60,
                    seconds = duration.getSeconds() % 60;

            if(hours > 0)
                str += hours   + "h " + minutes + "m " + seconds + "s";
            else if(minutes > 0)
                str += minutes + "m " + seconds + "s";
            else if(seconds > 0)
                str += seconds + "s";

            if(color == null) {
                if (duration.toMinutes() < 5)
                    color = TIME_WARNING;
                else
                    color = TIME_SAFE;
            }



            return new ColoredText(str, color);
        } else return ColoredText.EMPTY;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ((SimpleGUI)Main.getUI()).onWinLoad.run();
        exercises_view.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> updateIndex(newValue.intValue() + 1));

    }

    private static class CustomCellFactory implements Callback<ListView<ExerciseResponse>, ListCell<ExerciseResponse>> {
        @Override public ListCell<ExerciseResponse> call(ListView<ExerciseResponse> param) {
            return new ListCell<ExerciseResponse>() {

                @Override
                protected void updateItem(ExerciseResponse item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null)
                        setText(item.getParent().toString());
                    else
                        setText("");
                }
            };
        }
    }
}

package xyz.upperlevel.verifier.client.gui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.util.Callback;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import xyz.upperlevel.verifier.client.Assignment;
import xyz.upperlevel.verifier.client.Main;
import xyz.upperlevel.verifier.exercises.Exercise;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class AssignmentGUIController implements Initializable {
    @FXML
    public ListView<Exercise> exercises_view;

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

    private DoubleProperty property;

    @FXML
    private ObservableList<Exercise> exercises;

    private String assignmentId;

    public void init(String assignmentId, List<Exercise> exercises) {
        init(assignmentId, FXCollections.observableList(exercises));
    }

    public void init(String assignmentId, ObservableList<Exercise> exercises) {
        this.exercises = Objects.requireNonNull(exercises);
        this.assignmentId = assignmentId;

        exercises_view.setItems(exercises);
        exercises_view.setCellFactory(new CustomCellFactory());

        ex_max.setText(
                "" + exercises.size()
        );
        updateIndex();

        left_status.setText("");
        right_status.setText("");
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
            Main.onSendAssignment(new Assignment(assignmentId, exercises));
            close0();
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
    }

    @FXML
    public void onRequestTime() {
        throw new NotImplementedException();
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

    @Override public void initialize(URL location, ResourceBundle resources) {
        ((SimpleGUI)Main.getUI()).onWinLoad.run();
        exercises_view.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                updateIndex(newValue.intValue() + 1);
            }
        });
    }

    private static class CustomCellFactory implements Callback<ListView<Exercise>, ListCell<Exercise>> {
        @Override public ListCell<Exercise> call(ListView<Exercise> param) {
            return new ListCell<Exercise>() {

                @Override
                protected void updateItem(Exercise item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null)
                        setText(item.toString());
                    else
                        setText("");
                }
            };
        }
    }
}

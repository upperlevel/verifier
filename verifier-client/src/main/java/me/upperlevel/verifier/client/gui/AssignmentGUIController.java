package me.upperlevel.verifier.client.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import me.upperlevel.verifier.client.Assignment;
import me.upperlevel.verifier.client.Exercise;
import me.upperlevel.verifier.client.Main;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AssignmentGUIController implements Initializable{
    @FXML
    public ListView<Exercise> exercises_view;

    @FXML
    public TextField ex_number;
    @FXML
    public TextField ex_max;
    @FXML
    public TextField ex_percentage;
    @FXML
    public TextField ex_time;

    @FXML
    public AnchorPane ex_container;

    @FXML
    public TextField left_status;
    @FXML
    public TextField right_status;

    private int index = 0;

    @FXML
    private ObservableList<Exercise> exercises;

    public AssignmentGUIController(List<Exercise> exercises) {
        this.exercises = FXCollections.observableList(exercises);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        exercises_view.setItems(exercises);
        exercises_view.setCellFactory(new CustomCellFactory());

        ex_max.setText("" + exercises.size());
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
                "Ya sure?",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if(response == 1) {
            Main.onSendAssignment(new Assignment(exercises));
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

    private void updateIndex(int newIndex) {
        newIndex = newIndex < 0 ? 0 : newIndex > exercises.size() ? exercises.size() : newIndex;
        if(newIndex != index) {
            index = newIndex;
            updateIndex();
        }
    }

    public void updateIndex() {
        ex_number.setText("" + index);
        ex_percentage.setText(((index*100)/exercises.size()) + "%");
    }

    private static class CustomCellFactory implements Callback<ListView<Exercise>, ListCell<Exercise>> {
        @Override public ListCell<Exercise> call(ListView<Exercise> param) {
            return new ListCell<Exercise>() {

                @Override
                protected void updateItem(Exercise item, boolean empty) {
                    super.updateItem(item, empty);
                    setText("");
                    if (item != null)
                        setGraphic(item.getGraphics());
                    else
                        setGraphic(null);
                }
            };
        }
    }
}

package me.upperlevel.verifier.client.gui;

import javafx.application.Application;
import me.upperlevel.verifier.client.Assignment;
import me.upperlevel.verifier.proto.ErrorType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.swing.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SimpleGUI implements UI {

    protected Runnable onWinLoad;

    private final ConnChooseGUI gui = new ConnChooseGUI();

    @Override
    public void initWindows(Runnable onExe) {
        Application.launch(AssignmentGUI.class);
        this.onWinLoad = onExe;
    }

    @Override public void requestAddress(BiConsumer<String, Integer> callback) {
        gui.show(callback);
    }

    @Override
    public void chooseAssignment(List<Object> assignments, Consumer<Object> callback) {
        throw new NotImplementedException();
    }

    @Override
    public void openAssignment(Assignment assignment) {

    }

    @Override
    public void error(Exception e) {
        JOptionPane.showMessageDialog(
                null,
                e.getMessage(),
                "ERROR",
                JOptionPane.ERROR_MESSAGE
        );
    }

    @Override
    public void error(ErrorType error, String message) {
        JOptionPane.showMessageDialog(
                null,
                String.format("Error %1$s : \"%2$s\"", error.name(), message),
                "ERROR",
                JOptionPane.ERROR_MESSAGE
        );
    }
}

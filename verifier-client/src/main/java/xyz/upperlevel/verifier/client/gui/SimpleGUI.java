package xyz.upperlevel.verifier.client.gui;

import javafx.application.Application;
import javafx.application.Platform;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import xyz.upperlevel.verifier.client.AuthToken;
import xyz.upperlevel.verifier.client.assignments.AssignmentRequest;
import xyz.upperlevel.verifier.proto.ErrorType;

import javax.swing.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SimpleGUI implements UI {

    protected Runnable onWinLoad;

    private final ConnChooseGUI connGui = new ConnChooseGUI();

    private final AuthGUI authGUI = new AuthGUI();

    @Override
    public void initWindows(Runnable onExe) {
        this.onWinLoad = onExe;
        Platform.setImplicitExit(false);
        Application.launch(AssignmentGUI.class);
    }

    @Override public void requestAddress(BiConsumer<String, Integer> callback) {
        Platform.runLater(() -> connGui.show(callback));
    }

    @Override
    public void chooseAssignment(List<Object> assignments, Consumer<Object> callback) {
        throw new NotImplementedException();
    }

    @Override
    public void openAssignment(AssignmentRequest assignment) {
        Platform.runLater(() -> AssignmentGUI.getInstance().show(assignment));
    }

    @Override
    public void requestLogin(Consumer<AuthToken> callback) {
        Platform.runLater(() -> authGUI.show(callback));
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

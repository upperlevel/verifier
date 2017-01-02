package xyz.upperlevel.verifier.client.gui;

import xyz.upperlevel.verifier.client.AuthToken;
import xyz.upperlevel.verifier.client.assignments.AssignmentRequest;
import xyz.upperlevel.verifier.proto.ErrorType;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface UI {
    public void initWindows(Runnable onExe);

    public void requestAddress(BiConsumer<String, Integer> callback);

    public void chooseAssignment(List<Object> assignments, Consumer<Object> callback);

    public void openAssignment(AssignmentRequest assignment);

    public void requestLogin(Consumer<AuthToken> callback);

    public void error(Exception e);

    void error(ErrorType error, String message);
}

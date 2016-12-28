package me.upperlevel.verifier.client.gui;

import me.upperlevel.verifier.client.Assignment;
import me.upperlevel.verifier.proto.ErrorType;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface UI {
    public void initWindows(Runnable onExe);

    public void requestAddress(BiConsumer<String, Integer> callback);

    public void chooseAssignment(List<Object> assignments, Consumer<Object> callback);

    public void openAssignment(Assignment assignment);

    public void error(Exception e);

    void error(ErrorType error, String message);
}

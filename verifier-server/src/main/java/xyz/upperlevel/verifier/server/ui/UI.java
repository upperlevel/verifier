package xyz.upperlevel.verifier.server.ui;

import xyz.upperlevel.verifier.packetlib.simple.SimpleServer.SimpleServerOptions;
import xyz.upperlevel.verifier.proto.ErrorType;

import java.util.function.BiConsumer;

public interface UI {
    public void init(final String[] args);

    public void askConnInfo(BiConsumer<Integer, SimpleServerOptions> callback);

    public void error(ErrorType type, String message);

    public void start();
}

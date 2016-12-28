package me.upperlevel.verifier.client.conn;

import me.upperlevel.verifier.client.PacketListener;
import me.upperlevel.verifier.packetlib.simple.PacketExecutorManager;
import me.upperlevel.verifier.packetlib.simple.SimpleClient;
import me.upperlevel.verifier.proto.*;

import java.util.List;

public class ConnectionHandler implements Connection {
    private SimpleClient client = null;
    private PacketListener listener;


    @Override
    public void init(String ip, int port) throws Exception{
        client = new SimpleClient(ip, port);
        client.start();
        registerExecutor();
    }

    protected void registerExecutor() {
        PacketExecutorManager executor = client.getExecutorManager();
        executor.register(
                AssignmentPacket.class,
                p -> listener.onAsignment(p.getExercises())
        );
        executor.register(
                ErrorPacket.class,
                p -> listener.onError(p.getType(), p.getMessage())
        );
        executor.register(
                ExerciseTypePacket.class,
                p -> listener.onExerciseType(p.getName(), p.getFileData())
        );
    }

    @Override
    public void shutdown() {
        client.shutdown();
    }

    @Override
    public void sendExerciseTypeRequest(String name) {
        client.getChannel().writeAndFlush(new ExerciseTypePacket(name));
    }

    @Override
    public void sendLogin(String username, char[] passw, String clazz) {
        client.getChannel().writeAndFlush(new LoginPacket(clazz, username, passw));
    }

    @Override
    public void sendAssignment(List<ExerciseData> exercises) {
        client.getChannel().writeAndFlush(new AssignmentPacket(exercises));
    }

    @Override
    public void sendError(ErrorType type, String error) {
        client.getChannel().writeAndFlush(new ErrorPacket(type, error));
    }

    @Override
    public ConnectionHandler setListener(PacketListener listener) {
        this.listener = listener;
        return this;
    }
}

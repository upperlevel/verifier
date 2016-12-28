package me.upperlevel.verifier.client.conn;

import me.upperlevel.verifier.client.PacketListener;
import me.upperlevel.verifier.proto.ErrorType;
import me.upperlevel.verifier.proto.ExerciseData;

import java.util.List;

public interface Connection {
    public void init(String ip, int port) throws Exception;

    public void shutdown();



    public void sendExerciseTypeRequest(String name);

    public void sendLogin(String username, char[] passw, String clazz);

    public void sendAssignment(List<ExerciseData> exercises);

    public void sendError(ErrorType type, String error);

    Connection setListener(PacketListener listener);
}

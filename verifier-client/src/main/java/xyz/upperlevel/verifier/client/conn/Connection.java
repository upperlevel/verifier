package xyz.upperlevel.verifier.client.conn;

import xyz.upperlevel.verifier.client.PacketListener;
import xyz.upperlevel.verifier.proto.ErrorType;
import xyz.upperlevel.verifier.proto.ExerciseData;

import java.util.List;

public interface Connection {
    public void init(String ip, int port) throws Exception;

    public void shutdown();



    public void sendExerciseTypeRequest(String name);

    public void sendLogin(String clazz, String username, char[] passw);

    public void sendAssignment(String id, List<ExerciseData> exercises);

    public void sendError(ErrorType type, String error);

    Connection setListener(PacketListener listener);

    void sendTimeRequest();
}

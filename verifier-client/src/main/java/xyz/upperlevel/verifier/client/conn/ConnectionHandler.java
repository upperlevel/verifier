package xyz.upperlevel.verifier.client.conn;

import xyz.upperlevel.verifier.client.PacketListener;
import xyz.upperlevel.verifier.packetlib.simple.PacketExecutorManager;
import xyz.upperlevel.verifier.packetlib.simple.SimpleClient;
import xyz.upperlevel.verifier.proto.*;

import java.util.List;

public class ConnectionHandler implements Connection {
    private SimpleClient client = null;
    private PacketListener listener;


    @Override
    public void init(String ip, int port) throws Exception{
        client = new SimpleClient(ip, port);
        ProtocolUtils.registerDefPackets(client.getPacketManager());
        registerExecutor();
        client.start();
    }

    protected void registerExecutor() {
        PacketExecutorManager executor = client.getExecutorManager();
        executor.register(
                AssignmentPacket.class,
                p -> listener.onAsignment(p.getId(), p.getExercises())
        );
        executor.register(
                ErrorPacket.class,
                p -> listener.onError(p.getType(), p.getMessage())
        );
        executor.register(
                ExerciseTypePacket.class,
                p -> listener.onExerciseType(p.getName(), p.getFileData())
        );
        executor.register(
                TimePacket.class,
                p -> listener.onTime(p.getType(), p.getTime())
        );
    }

    @Override
    public void shutdown() {
        if(client != null)
            client.shutdown();
    }

    @Override
    public void sendExerciseTypeRequest(String name) {
        client.getChannel().writeAndFlush(new ExerciseTypePacket(name));
    }

    @Override
    public void sendLogin(String clazz, String username, char[] passw) {
        client.getChannel().writeAndFlush(new LoginPacket(clazz, username, passw));
    }

    @Override
    public void sendAssignment(String id, List<ExerciseData> exercises) {
        client.getChannel().writeAndFlush(new AssignmentPacket(id, exercises));
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

    @Override
    public void sendTimeRequest() {
        client.getChannel().writeAndFlush(new TimePacket(TimePacket.PacketType.REQUEST, null));
    }
}

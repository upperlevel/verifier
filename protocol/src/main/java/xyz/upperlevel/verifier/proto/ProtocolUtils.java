package xyz.upperlevel.verifier.proto;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import xyz.upperlevel.verifier.packetlib.PacketManager;
import xyz.upperlevel.verifier.proto.ssl.SslClientContext;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.function.Function;

public class ProtocolUtils {
    public static void registerDefPackets(PacketManager manager) {
        manager.register(
                AssignmentPacket.HANDLER,
                ErrorPacket.HANDLER,
                LoginPacket.HANDLER,
                ExerciseTypePacket.HANDLER,
                TimePacket.HANDLER
        );
    }

    public static Function<ByteBufAllocator, SslHandler> getSslClient() {
        SslContext context = SslClientContext.CONTEXT;
        return context != null ? context::newHandler : null;
    }

    public static Function<ByteBufAllocator, SslHandler> getSslServer(File certPath, File keyPath, String keyPassw) throws SSLException {
        if(certPath.exists() || keyPath.exists()) {
            return SslContextBuilder
                    .forServer(certPath, keyPath, keyPassw)
                    .build()::newHandler;
        } else return null;
    }


}

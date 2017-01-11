package xyz.upperlevel.verifier.proto;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import xyz.upperlevel.verifier.proto.protobuf.AnyPacket;
import xyz.upperlevel.verifier.proto.ssl.SslClientContext;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.function.Function;

public class ProtocolUtils {
    public static void registerDefPackets(ChannelPipeline pipeline) {
        pipeline.addLast("deframer", new ProtobufVarint32FrameDecoder());
        pipeline.addLast("protobuf-decoder", new ProtobufDecoder(AnyPacket.Any.getDefaultInstance()));
        pipeline.addLast("demux", new ProtobufAdapter.AnyDecoder());

        pipeline.addLast("framer", new ProtobufVarint32LengthFieldPrepender());
        pipeline.addLast("protobuf-encoder", new ProtobufEncoder());
        pipeline.addLast("mux", new ProtobufAdapter.AnyEncoder());
    }

    public static Function<ByteBufAllocator, SslHandler> getSslClient() {
        SslContext context = SslClientContext.CONTEXT;
        return context != null ? context::newHandler : null;
    }

    public static Function<ByteBufAllocator, SslHandler> getSslServer(File certPath, File keyPath, String keyPassw) throws SSLException {
        if (certPath.exists() || keyPath.exists()) {
            return SslContextBuilder
                    .forServer(certPath, keyPath, keyPassw)
                    .build()::newHandler;
        } else return null;
    }


}

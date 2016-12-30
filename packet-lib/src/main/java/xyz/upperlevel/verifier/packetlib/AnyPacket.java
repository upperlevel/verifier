package xyz.upperlevel.verifier.packetlib;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.Getter;
import xyz.upperlevel.verifier.packetlib.PacketManager.PacketTypeLength;

import java.util.List;

public class AnyPacket {
    @Getter
    private int type;
    @Getter
    private byte[] data;

    public AnyPacket(int id, byte[] data) {
        this.type = id;
        this.data = data;
    }

    protected static final class TypeEncoder extends MessageToMessageEncoder<AnyPacket> {
        public final PacketTypeLength bytes;

        public TypeEncoder(PacketTypeLength bytes) {
            this.bytes = bytes;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void encode(ChannelHandlerContext channelHandlerContext, AnyPacket in, List<Object> out) throws Exception {
            out.add(Unpooled.copiedBuffer(bytes.toByteArray(in.getType()), in.data));
        }
    }

    protected static final class TypeDecoder extends MessageToMessageDecoder<ByteBuf> {
        public final PacketTypeLength bytes;

        public TypeDecoder(PacketTypeLength bytes) {
            this.bytes = bytes;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
            out.add(
                    new AnyPacket(
                            bytes.toInt(read(in, bytes.bytes)),
                            read(in)
                    )
            );
        }
    }

    private static void checkBytes(int bytes) {
        if(bytes < 1 || bytes > 4)
            throw new IllegalArgumentException("The type size must be between 1 and 4 (inclusive)");
    }

    private static byte[] read(ByteBuf buffer, int bytes) {
        byte[] res = new byte[bytes];
        buffer.readBytes(res);
        return res;
    }

    private static byte[] read(ByteBuf buffer) {
        return read(buffer, buffer.readableBytes());
    }
}

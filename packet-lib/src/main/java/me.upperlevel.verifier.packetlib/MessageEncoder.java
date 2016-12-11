package me.upperlevel.verifier.packetlib;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import me.upperlevel.verifier.packetlib.PacketManager.PacketHandler;

@AllArgsConstructor
public class MessageEncoder extends MessageToByteEncoder<Object> {
    private final PacketManager manager;

    @Override
    @SuppressWarnings("unchecked")
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        PacketHandler handler = manager.getHandlerContinued(msg.getClass());
        if (handler != null) {
            out.writeShort(handler.getId());
            byte[] data = handler.encode(msg);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}

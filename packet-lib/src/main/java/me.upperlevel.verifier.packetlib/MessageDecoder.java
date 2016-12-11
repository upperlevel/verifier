package me.upperlevel.verifier.packetlib;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class MessageDecoder extends ByteToMessageDecoder {
    private final PacketManager manager;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        short id = in.readShort();
        PacketManager.PacketHandler<?> handler = manager.getHandler(id);
        if(handler != null) {
            int size = in.readInt();
            byte[] message = new byte[size];
            in.readBytes(message);
            out.add(handler.decode(message));
        }
    }
}

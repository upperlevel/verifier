package xyz.upperlevel.verifier.proto;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import xyz.upperlevel.verifier.proto.protobuf.*;

import java.util.List;


/**
 * I hate this class, really, it's just a big repetition, but I didn't found any other solution to this
 */
public class ProtobufAdapter {

    public static class AnyDecoder extends MessageToMessageDecoder<AnyPacket.Any> {
        @Override
        protected void decode(ChannelHandlerContext ctx, AnyPacket.Any msg, List<Object> out) throws Exception {
            switch (msg.getMexCase()) {
                case ASSIGNMENT:
                    out.add(msg.getAssignment());
                    break;
                case ERROR:
                    out.add(msg.getError());
                    break;
                case LOGIN:
                    out.add(msg.getLogin());
                    break;
                case TIME:
                    out.add(msg.getTime());
                    break;
                case MEX_NOT_SET:
                    System.err.println("[WARN]Received empty protobuf message!");
                    break;
                default:
                    System.err.println("[WARN]Received unknown protobuf message: " + msg.getMexCase().name());
            }
        }
    }

    public static class AnyEncoder extends MessageToMessageEncoder<Object> {
        @Override
        protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
            AnyPacket.Any.Builder packet = AnyPacket.Any.newBuilder();

            if     (msg instanceof AssignmentPacket.Assignment)
                packet.setAssignment((AssignmentPacket.Assignment) msg);
            else if(msg instanceof ErrorPacket.Error)
                packet.setError((ErrorPacket.Error) msg);
            else if(msg instanceof LoginPacket.Login)
                packet.setLogin((LoginPacket.Login) msg);
            else if(msg instanceof TimePacket.Time)
                packet.setTime((TimePacket.Time) msg);
            else
                System.err.println("[WARN] AnyEncoder received an unhandled packet type: " + msg.getClass().getSimpleName());

            out.add(packet.build());
        }

        @Override
        public boolean acceptOutboundMessage(Object msg) throws Exception {
            return  msg instanceof AssignmentPacket.Assignment ||
                    msg instanceof ErrorPacket.Error ||
                    msg instanceof LoginPacket.Login ||
                    msg instanceof TimePacket.Time;
        }
    }
}

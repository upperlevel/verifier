package me.upperlevel.verifier.packetlib;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.Getter;
import me.upperlevel.verifier.packetlib.collections.IdList;
import me.upperlevel.verifier.packetlib.exceptions.NotEnoughIdsException;
import me.upperlevel.verifier.packetlib.proto.HandshakePacket;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PacketManager {
    public final int minId;
    public final int maxId;

    public final long maxPackets;

    private int nextId;


    public final Map<String, PacketHandler<?>> handlers = new HashMap<>();
    public final Map<Class<?>, PacketHandler<?>> class_mapped = new HashMap<>();
    public List<PacketHandler<?>> id_mapped = new IdList<>(1024);

    public final MessageToMessageEncoder<Object> encoder = createEncoder();
    public final MessageToMessageDecoder<AnyPacket> decoder = createDecoder();
    public final SimpleChannelInitializer initializer;

    @Getter
    private final SimpleConnectionOptions options;
    @Getter
    private SideType sideType;

    public PacketManager(SideType type, SimpleConnectionOptions options) {
        PacketTypeLength length = options.getTypeBytes();
        minId = length.min;
        maxId = length.max;
        maxPackets = length.length;
        nextId = minId;

        this.sideType = type;
        this.options = options;

        this.initializer = new SimpleChannelInitializer(this);
        register(HandshakePacket.HANDLER);
    }

    public PacketManager(SideType type) {
        this(type, SimpleConnectionOptions.DEFAULT);
    }


    protected void map(PacketHandler<?> in) {
        in.setId(nextId++);

        class_mapped.put(in.getHandled(), in);
        id_mapped.set(in.getId() - minId, in);
    }

    public void register(PacketHandler<?>... handlers) {
        for(PacketHandler<?> handler : handlers)
            register(handler);
    }

    public boolean register(PacketHandler<?> handler) {
        checkIdRange();
        if(handlers.putIfAbsent(handler.getName(), handler) == null) {
            map(handler);
            return true;
        } else return false;
    }

    public <P> boolean register(String name, Class<P> clazz, Function<byte[], P> encoder, Function<P, byte[]> decoder) {
        return register( PacketHandler.from(name, clazz, encoder, decoder));
    }

    public PacketHandler<?> get(String name) {
        return handlers.get(name);
    }

    public PacketHandler<?> getOrDefault(String name, PacketHandler<?> def) {
        return handlers.getOrDefault(name, def);
    }

    private void checkIdRange() {
        if(nextId > maxId)
            throw new NotEnoughIdsException();
    }

    public void onHandshake(HandshakePacket handshake) {
        List<String> pkts = handshake.getPackets();

        if(pkts.size() != handlers.size() - 1)
            throw new IllegalStateException("The server packets are different from the clients: (server:" + pkts.size() + ", client:" + (handlers.size() + 1) + ")");

        List<PacketHandler<?>> id_mpd = new IdList<>();
        id_mpd.set(0, HandshakePacket.HANDLER);
        final int size = pkts.size();
        for (int i = 0; i < size; i++) {
            String str = pkts.get(i);
            PacketHandler<?> handler = handlers.get(str);
            if(handler == null)
                throw new IllegalStateException("This client doesn't have the packet: \"" + str + "\"");
            handler.setId(i + 1);
            id_mpd.set(handler.getId(), handler);
        }

        this.id_mapped = id_mpd;
    }

    public HandshakePacket createHandshake() {
        return new HandshakePacket(new ArrayList<>(handlers.keySet()).subList(1, handlers.size()));
    }

    public boolean isServer() {
        return sideType == SideType.SERVER;
    }

    public boolean isClient() {
        return sideType == SideType.CLIENT;
    }

    public MessageToMessageDecoder<AnyPacket> createDecoder() {
        return new PacketDecoder();
    }

    public MessageToMessageEncoder<Object> createEncoder() {
        return new PacketEncoder();
    }

    public enum PacketTypeLength {
        BYTE(1) {
            @Override public int toInt(byte[] in) {
                return in[0];
            }

            @Override public byte[] toByteArray(int in) {
                return new byte[]{
                        (byte) in
                };
            }
        },
        SHORT(2) {
            @Override public int toInt(byte[] in) {
                return ByteBuffer.wrap(in).getShort();
            }

            @Override public byte[] toByteArray(int in) {
                return new byte[]{
                        (byte) (in >> 8),
                        (byte) in
                };
            }
        },
        INT(4) {
            @Override public int toInt(byte[] in) {
                return ByteBuffer.wrap(in).getInt();
            }

            @Override public byte[] toByteArray(int in) {
                return new byte[]{
                        (byte) (in >> 24),
                        (byte) (in >> 16),
                        (byte) (in >> 8),
                        (byte) in
                };
            }
        };

        public final int bytes;
        public final int min, max;
        public final long length;

        PacketTypeLength(int bytes) {
            this.bytes = bytes;
            min = min(bytes);
            max = max(bytes);
            length = (((long) min) - max) + 1L;
        }

        public static int max(int bits) {
            return (int) Math.pow(2, bits * 8 - 1) - 1;
        }

        public static int min(int bits) {
            return (int) -Math.pow(2, bits * 8 - 1);
        }

        public static PacketTypeLength getFromBytes(int bytes) {
            switch (bytes) {
                case 1:
                    return BYTE;
                case 2:
                    return SHORT;
                case 4:
                    return INT;
                default:
                    return null;
            }
        }

        public abstract int toInt(byte[] in);

        public abstract byte[] toByteArray(int in);
    }


    protected final class PacketEncoder extends MessageToMessageEncoder<Object> {
        @Override
        @SuppressWarnings("unchecked")
        protected void encode(ChannelHandlerContext channelHandlerContext, Object in, List<Object> out) throws Exception {
            PacketHandler<?> handler = class_mapped.get(in.getClass());
            if(handler != null)
                out.add(new AnyPacket(handler.getId(), ((PacketHandler)handler).encode(in)));
            else
                System.out.println("WARN: PacketEncoder-> unknown packet received (class:" + in.getClass().getName() + "):" + class_mapped.keySet());
        }
    }

    protected final class PacketDecoder extends MessageToMessageDecoder<AnyPacket> {
        @Override
        @SuppressWarnings("unchecked")
        protected void decode(ChannelHandlerContext channelHandlerContext, AnyPacket in, List<Object> out) throws Exception {
            PacketHandler<?> handler = id_mapped.get(in.getType() - minId);
            if(handler != null)
                out.add(handler.decode(in.getData()));
            else
                System.out.println("WARN: PacketDecoder-> unknown packet received: (id:" + in.getType() + ")");
        }
    }

    public enum SideType {
        CLIENT, SERVER;
    }
}

package me.upperlevel.verifier.packetlib;

import me.upperlevel.verifier.packetlib.defs.HandshakePacket;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 *
 * @param <S> The sender type (who sends the message)
 */
public class PacketManager<S> {
    private final short MIN_VALUE = Short.MIN_VALUE;
    private short nextID = Short.MIN_VALUE;

    public PacketHandler<?>[] packets = new PacketHandler[Short.MAX_VALUE - Short.MIN_VALUE];

    public Map<Class<?>, PacketHandler<?>> class_mapped = new HashMap<>();

    private final MessageListenerManager<S> messageListenerManager;

    public PacketManager(Class<S> senderType) {
        messageListenerManager = new MessageListenerManager<>(senderType);
        setupDefs();
    }

    private void setupDefs() {
        register(HandshakePacket.HANDLER);
    }


    public PacketHandler<?> getId(short id) {
        return packets[id - MIN_VALUE];
    }

    public void setId(short id, PacketHandler<?> packet){
        packets[id - MIN_VALUE] = packet;
    }

    public <T> void register(String name, Class<T> clazz, Function<T, byte[]> encoder, Function<byte[], T> decoder) {
        register(new SimpleHandler<>(name, clazz, encoder, decoder));
    }

    public void register(PacketHandler<?> handler) {
        setId(nextID, handler);
        handler.registerHandler(nextID);
        nextID++;
        class_mapped.put(handler.getClazz(), handler);
    }

    public byte[] encodeAll() {
        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        for (short id = 0; id < packets.length; id++) {
            out.write(id);
            try {
                out.write(packets[id].getName().getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            out.write((byte) '\0');
        }
        return out.toByteArray();
    }


    /**
     * @see MessageListenerManager#addListener(Object)
     */
    public void addListener(Object listener) {
        messageListenerManager.addListener(listener);
    }

    /**
     * @see MessageListenerManager#addListener(Class, BiConsumer)
     */
    public <T> void addListener(Class<T> type, BiConsumer<S, T> consumer) {
        messageListenerManager.addListener(type, consumer);
    }

    /**
     * @see MessageListenerManager#addListener(Class, Consumer)
     */
    public <T> void addListener(Class<T> type, Consumer<T> consumer) {
        messageListenerManager.addListener(type, consumer);
    }

    /**
     * @see MessageListenerManager#call(S, Object)
     */
    public void onMessageReceive(S sender, Object msg) {
        messageListenerManager.call(sender, msg);
    }


    public PacketHandler<?> getHandler(Object msg) {
        Class<?> clazz = msg.getClass();
        return class_mapped.get(clazz);
    }

    public PacketHandler<?> getHandler(int id) {
        return getId((short) id);
    }

    public PacketHandler<?> getHandler(Class<?> clazz) {
        return class_mapped.get(clazz);
    }

    public PacketHandler<?> getHandlerContinued(Class<?> clazz) {
        PacketHandler<?> handler = getHandler(clazz);
        return handler == null && clazz.getSuperclass() != null ? getHandlerContinued(clazz.getSuperclass()) : handler;
    }

    public HandshakePacket getHandshake() {
        List<String> packets_name = new ArrayList<>(nextID - Short.MIN_VALUE);

        final int max = nextID - MIN_VALUE;
        for(short i = 0; i < max; i++)
            packets_name.add(packets[i].getName());

        return new HandshakePacket(packets_name);
    }

    public void setHandshake(HandshakePacket packet) {
        PacketHandler<?>[] new_packets = new PacketHandler[packets.length];
        String[] names = packet.packets;
        for (int i = 0; i < names.length; i++) {
            new_packets[i] = getFromName(names[i]);
            if(new_packets[i] == null)
                System.err.println("WARNING! packet not registered!");
            else
                new_packets[i].registerHandler((short) (i + MIN_VALUE));
        }
        packets = new_packets;
    }

    private PacketHandler<?> getFromName(String name) {
        Objects.requireNonNull(name);
        final int max = nextID - MIN_VALUE;
        for (int i = 0; i < max; i++) {
            if(name.equals(packets[i].name))
                return packets[i];
        }
        return null;
    }

    public static abstract class PacketHandler<T> {
        public static final int UNDEFINED = Integer.MAX_VALUE;
        public final String name;
        public final Class<T> clazz;
        private int id = UNDEFINED;

        public PacketHandler(String name, Class<T> clazz) {
            if(name == null)
                throw new IllegalArgumentException("The argument name is null!");
            else if(name.length() > 128)
                throw new IllegalArgumentException("Name too long!");
            this.name = name;
            this.clazz = requireNonNull(clazz);
        }

        public boolean isRegistered() {
            return id != UNDEFINED;
        }

        private void registerHandler(short id) {
            this.id = id;
        }

        public short getId() {
            if(isRegistered())
                throw new IllegalStateException();
            return (short)id;
        }

        public String getName() {
            return name;
        }

        public abstract byte[] encode(T decoded);

        public abstract T decode(byte[] encoded);

        public Class<T> getClazz() {
            return clazz;
        }
    }

    private static class SimpleHandler<T> extends PacketHandler<T> {
        private final Function<T, byte[]> encoder;
        private final Function<byte[], T> decoder;


        public SimpleHandler(String name, Class<T> clazz, Function<T, byte[]> encoder, Function<byte[], T> decoder) {
            super(name, clazz);
            this.encoder = requireNonNull(encoder);
            this.decoder = requireNonNull(decoder);
        }

        @Override
        public byte[] encode(T decoded) {
            return encoder.apply(decoded);
        }

        @Override
        public T decode(byte[] encoded) {
            return decoder.apply(encoded);
        }
    }
}

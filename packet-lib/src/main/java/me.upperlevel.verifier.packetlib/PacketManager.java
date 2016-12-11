package me.upperlevel.verifier.packetlib;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 *
 * @param <S> The sender type (who sends the message)
 */
public class PacketManager<S> {
    private short nextID = Short.MIN_VALUE;

    public PacketHandler<?>[] packets = new PacketHandler[Short.MAX_VALUE - Short.MIN_VALUE];

    public Map<Class<?>, PacketHandler<?>> class_mapped = new HashMap<>();

    private final MessageListenerManager<S> messageListenerManager;

    public PacketManager(Class<S> senderType) {
        messageListenerManager = new MessageListenerManager<>(senderType);
    }

    public <T> void register(String name, Class<T> clazz, Function<T, byte[]> encoder, Function<byte[], T> decoder) {
        register(new SimpleHandler<>(name, clazz, encoder, decoder));
    }

    public void register(PacketHandler<?> handler) {
        packets[nextID] =  handler;
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
        return packets[id];
    }

    public PacketHandler<?> getHandler(Class<?> clazz) {
        return class_mapped.get(clazz);
    }

    public PacketHandler<?> getHandlerContinued(Class<?> clazz) {
        PacketHandler<?> handler = getHandler(clazz);
        return handler == null && clazz.getSuperclass() != null ? getHandlerContinued(clazz.getSuperclass()) : handler;
    }

    public static abstract class PacketHandler<T> {
        public final String name;
        public final Class<T> clazz;
        private int id = Short.MAX_VALUE;

        public PacketHandler(String name, Class<T> clazz) {
            this.name = requireNonNull(name);
            this.clazz = requireNonNull(clazz);
        }

        public boolean isRegistered() {
            return id != Short.MAX_VALUE;
        }

        private void registerHandler(short id) {
            if(isRegistered())
                throw new IllegalStateException();
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

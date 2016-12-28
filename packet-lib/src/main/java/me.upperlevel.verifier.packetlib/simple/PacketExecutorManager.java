package me.upperlevel.verifier.packetlib.simple;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import me.upperlevel.verifier.packetlib.PacketManager;

import java.util.*;
import java.util.function.Consumer;

public class PacketExecutorManager {
    private final PacketManager packetManager;

    private Map<Class, Executor> handlers = new HashMap<>();

    private List<Consumer<ChannelHandlerContext>> onChannelConnect = new ArrayList<>(),
            onChannelDisconnect = new ArrayList<>();

    public PacketExecutorManager(PacketManager packetManager) {
        this.packetManager = packetManager;
    }


    @SuppressWarnings("unchecked")
    public <T> void register(Class<T> packetClazz, Executor<T> executor) {//Ehm, sorry for the one liner, it was stronger than me
        //So, if the hashmap doesn't contain a value mapped to packetClazz, then executor gets pushed,
        //If the map contains a value and it is a CollectionExecutor, the object gets added to it
        //If the map contains a value but it's not a CollectionExecutor, a CollectionExecutor is created with the two executors
        handlers.compute(
                packetClazz,
                (clazz, exe) -> exe == null
                        ? executor
                        : exe instanceof CollectionExecutor
                                ? ((CollectionExecutor) exe).push(executor)
                                : new CollectionExecutor(exe, executor)
        );
    }

    public <T> void register(Class<T> packetClazz, Consumer<T> handler) {
        register(packetClazz, (a, b) -> handler.accept(b));
    }

    @SuppressWarnings("unchecked")
    public boolean execute(ChannelHandlerContext context, Object packet) {
        Executor executor = handlers.get(packet.getClass());
        if(executor != null) {
            executor.execute(context, packet);
            return true;
        }
        return false;
    }

    public void registerChannelConnect(Consumer<ChannelHandlerContext> onConnect) {
        onChannelConnect.add(onConnect);
    }

    public void registerChannelDisconnect(Consumer<ChannelHandlerContext> onDisconnect) {
        onChannelDisconnect.add(onDisconnect);
    }

    public PacketCaller createCaller() {
        return new PacketCaller();
    }


    public interface Executor<T> {
        public void execute(ChannelHandlerContext context, T packet);
    }

    public class CollectionExecutor<T> implements Executor<T> {
        private final Collection<Executor<T>> executors;

        public CollectionExecutor(Collection<Executor<T>> executors) {
            this.executors = executors;
        }

        @SafeVarargs public CollectionExecutor(Executor<T>... arr) {
            this(new ArrayList<>());
            if(arr.length != 0)
                executors.addAll(Arrays.asList(arr));
        }

        @Override public void execute(ChannelHandlerContext context, T packet) {
            for(Executor<T> executor : executors)
                executor.execute(context, packet);
        }

        public CollectionExecutor<T> push(Executor<T> executor) {
            executors.add(executor);
            return this;
        }

        public boolean contains(Executor<T> executor) {
            return executors.contains(executor);
        }

        public boolean pop(Executor<T> executor) {
            return executors.remove(executor);
        }
    }

    public class PacketCaller extends ChannelInboundHandlerAdapter {
        private final boolean autoRelease;

        public PacketCaller(boolean autoRelease) {
            this.autoRelease = autoRelease;
        }

        public PacketCaller() {
            this(true);
        }


        @Override
        public void handlerAdded(ChannelHandlerContext context) {
            context.channel().writeAndFlush(packetManager.createHandshake());
            onChannelConnect.forEach(c -> c.accept(context));
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext context) {
            onChannelDisconnect.forEach(c -> c.accept(context));
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            boolean release = true;

            try {
                if(!execute(ctx, msg))  {
                    release = false;
                    ctx.fireChannelRead(msg);
                }
            } finally {
                if(this.autoRelease && release)
                    ReferenceCountUtil.release(msg);
            }
        }
    }
}

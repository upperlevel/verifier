package me.upperlevel.verifier.packetlib;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HandlerManager<S> {
    public Map<Class<?>, List<Handler<S, ?>>> handlers = new HashMap<>(30);
    public Logger logger = Logger.getLogger("HandlerManager");

    @SuppressWarnings("unchecked")
    public void register(MessageHandlerContainer obj) {
        for(Method method : obj.getClass().getDeclaredMethods()) {
            if(method.isAnnotationPresent(MessageHandler.class)) {
                Handler handler;
                int index = 0;

                if(method.getParameterCount() == 1)
                    handler = new ClassHandler<>(method, obj);
                else if(method.getParameterCount() == 2)
                    handler = new SenderClassHandler<>(method, obj);
                else {
                    logger.warning("The method \"" + method.getName() + "\" in class \"" + obj.getClass().getSimpleName() + "\" is annotated incorrectly" );
                    continue;
                }
                getOrCreate(method.getParameterTypes()[index]).add(handler);
            }
        }
    }

    public <T> void register(Class<T> clazz, Handler<S, T> handler) {
        getOrCreate(clazz).add(handler);
    }



    public int invoke(S sender, Object message) {
        Class<?> type = message.getClass();
        int invoked;
        while(
                        (invoked = invoke(sender, message, type)) == 0 &&
                        type.getSuperclass() != null
                ) type = type.getSuperclass();
        return invoked;
    }

    @SuppressWarnings("unchecked")
    public <T> int invoke(S sender, T message, Class<?> type) {
        List<Handler<S, T>> found = (List)handlers.get(type);
        if(found == null || found.size() == 0)
            return 0;
        for(Handler<S, T> handler : found)
            try {
                handler.handle(sender, message);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error while invoking message", e);
            }
        return found.size();
    }

    @SuppressWarnings("unchecked")
    private <T> List<Handler<S, T>> getOrCreate(Class<T> clazz) {
        List<Handler<S, T>> res = (List)handlers.computeIfAbsent(clazz, k -> new ArrayList<>());
        if(res == null)
            throw new IllegalStateException("computeIfAbsent is not working!");
        return res;
    }

    public interface Handler<S, T> {
        public void handle(S sender, T message) throws Exception;
    }

    private static class ClassHandler<S> implements Handler<S, Object> {
        private final Method method;
        private final Object instance;

        private ClassHandler(Method method, Object instance) {
            this.method = method;
            this.instance = instance;
        }

        @Override
        public void handle(S sender, Object message) throws InvocationTargetException, IllegalAccessException {
            method.invoke(instance, message);
        }
    }

    private static class SenderClassHandler<S> implements Handler<S, Object> {
        private final Method method;
        private final Object instance;

        private SenderClassHandler(Method method, Object instance) {
            this.method = method;
            this.instance = instance;
        }

        @Override
        public void handle(S sender, Object message) throws InvocationTargetException, IllegalAccessException {
            method.invoke(instance, sender, message);
        }
    }
}

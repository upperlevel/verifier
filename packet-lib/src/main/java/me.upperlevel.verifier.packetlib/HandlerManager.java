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
    public Map<Class<?>, List<Handler<S>>> handlers = new HashMap<>(30);
    public Logger logger = Logger.getLogger("HandlerManager");

    public void register(Object obj) {
        for(Method method : obj.getClass().getDeclaredMethods()) {
            if(method.isAnnotationPresent(MessageHandler.class)) {
                Handler<S> handler;
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

    public int invoke(S sender, Object message) {
        Class<?> type = message.getClass();
        int invoked;
        while(
                        (invoked = invoke(sender, message, type)) == 0 &&
                        type.getSuperclass() != null
                ) type = type.getSuperclass();
        return invoked;
    }

    public int invoke(S sender, Object message, Class<?> type) {
        List<Handler<S>> found = handlers.get(type);
        if(found == null || found.size() == 0)
            return 0;
        for(Handler<S> handler : found)
            try {
                handler.handle(sender, message);
            } catch (InvocationTargetException | IllegalAccessException e) {
                logger.log(Level.SEVERE, "Error while invoking message", e);
            }
        return found.size();
    }

    private List<Handler<S>> getOrCreate(Class<?> clazz) {
        List<Handler<S>> res = handlers.computeIfAbsent(clazz, k -> new ArrayList<>());
        if(res == null)
            throw new IllegalStateException("computeIfAbsent is not working!");
        return res;
    }

    private interface Handler<S> {
        public void handle(S server, Object message) throws InvocationTargetException, IllegalAccessException;
    }

    private static class ClassHandler<S> implements Handler<S> {
        private final Method method;
        private final Object instance;

        private ClassHandler(Method method, Object instance) {
            this.method = method;
            this.instance = instance;
        }

        @Override
        public void handle(S server, Object message) throws InvocationTargetException, IllegalAccessException {
            method.invoke(instance, message);
        }
    }

    private static class SenderClassHandler<S> implements Handler<S> {
        private final Method method;
        private final Object instance;

        private SenderClassHandler(Method method, Object instance) {
            this.method = method;
            this.instance = instance;
        }

        @Override
        public void handle(S server, Object message) throws InvocationTargetException, IllegalAccessException {
            method.invoke(instance, server, message);
        }
    }
}

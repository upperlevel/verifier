package me.upperlevel.verifier.packetlib;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageListenerManager<S> {
    public static Logger logger = Logger.getLogger("MessageListenerManager");

    private Map<Class<?>, Set<BiConsumer<S, Object>>> listeners = new HashMap<>(256);
    private final Class<S> senderType;

    public MessageListenerManager(Class<S> senderType) {
        this.senderType = senderType;
    }

    /**
     *
     * @param sender who sent the message
     * @param message the message itself
     * @return the number of listeners that got executed
     */
    public int call(S sender, Object message) {
        Set<BiConsumer<S, Object>> list = listeners.get(message.getClass());
        if(list == null) return 0;
        for (BiConsumer<S, Object> listener : list)
            listener.accept(sender, message);
        return list.size();
    }

    public void addListener(Object o) {
        addListener0(o, o.getClass());
    }

    /**
     * This adds a listener that gets called whenever the sender sends a message
     * @param type The class of the message
     * @param o the Function to call
     * @param <T> the type of the message
     */
    @SuppressWarnings("unchecked")//This is NOT unchecked! T extends Object! stupid java syntax
    public <T> void addListener(Class<T> type, BiConsumer<S, T> o) {
        getOrCreate(type).add((BiConsumer)o);
    }

    /**
     * This adds a listener that gets called whenever the sender sends a message.<br>
     * This differs from <code>addListener(Class, BiConsumer)</code> because this doesn't pass the sender tho the consumer.<br>
     * (For the speed maniacs) This is not faster than the other method, actually it's some ticks slower
     * @param type The class of the message
     * @param o the Function to call
     * @param <T> the type of the message
     */
    public <T> void addListener(Class<T> type, Consumer<T> o) {
        addListener(type, (sender, message) -> o.accept(message));
    }

    protected void addListener0(Object listener, Class<?> clazz) {
        if(clazz == Object.class || clazz == null)
            return;

        for(Method method : clazz.getDeclaredMethods()) {
            MessageHandler handler = method.getAnnotation(MessageHandler.class);
            if(handler != null) {
                if(method.getParameterCount() != 2) {
                    badParametersLog(method, clazz, "right paramaters (sender, type)");
                    continue;
                }
                Class<?>[] params = method.getParameterTypes();
                if(!params[0].isAssignableFrom(senderType))
                    badParametersLog(method, clazz, "right sender (? extends " + senderType.getSimpleName() + ')');
                getOrCreate(params[1]).add(createListener(listener, method));
            }
        }

        if((clazz = clazz.getSuperclass()) != Object.class && clazz != null) {
            addListener0(listener, clazz);
        }
    }

    protected Set<BiConsumer<S, Object>> getOrCreate(Class<?> type) {
        return listeners.computeIfAbsent(type, (t) -> new HashSet<>(20));
    }

    protected void badParametersLog(Method method, Class<?> clazz, String postFix) {
        logger.severe("The method " + method.getName() + " in the listener: " + clazz.getName() + " doesn't have the " + postFix);
    }

    protected Listener createListener(Object instance, Method method) {
        int paramCount = method.getParameterCount();
        if(paramCount == 1)
            return new TypeListener(instance, method);
        else if(paramCount == 2)
            return new SenderTypeListener(instance, method);
        else return null;
    }

    protected abstract class Listener implements BiConsumer<S, Object> {
        protected final Object instance;
        protected final Method method;

        public Listener(Object instance, Method method) {
            this.instance = instance;
            this.method = method;
            method.setAccessible(true);
        }
    }

    protected class TypeListener extends Listener {

        public TypeListener(Object instance, Method method) {
            super(instance, method);
        }

        public void accept(S sender, Object message){
            try {
                method.invoke(message);
            } catch (IllegalAccessException e) {
                logger.log(Level.SEVERE, "Cannot access " + method.getName() + " from " + method.getDeclaringClass().getName(), e);
            } catch (InvocationTargetException e) {
                logger.log(Level.SEVERE, "Exception trown from " + method.getName() + " : " + method.getDeclaringClass().getName(), e);
            }
        }
    }

    protected class SenderTypeListener extends Listener {

        public SenderTypeListener(Object instance, Method method) {
            super(instance, method);
        }

        public void accept(S sender, Object message){
            try {
                method.invoke(sender, message);
            } catch (IllegalAccessException e) {
                logger.log(Level.SEVERE, "Cannot access " + method.getName() + " from " + method.getDeclaringClass().getName(), e);
            } catch (InvocationTargetException e) {
                logger.log(Level.SEVERE, "Exception trown from " + method.getName() + " : " + method.getDeclaringClass().getName(), e);
            }
        }
    }
}

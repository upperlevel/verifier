package xyz.upperlevel.verifier.client;

import xyz.upperlevel.verifier.exercises.ExerciseType;
import xyz.upperlevel.verifier.exercises.ExerciseTypeManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class ExerciseUtil {
    protected static final ExecutorService waiters = Executors.newSingleThreadExecutor();

    protected static Map<String, Consumer<ExerciseType<?>>> listeners = new HashMap<>();

    static {
        Main.getExerciseManager().registerListener(ExerciseUtil::onExType);
    }


    protected static void onExType(ExerciseType<?> reg) {
        Consumer<ExerciseType<?>> listener = listeners.remove(reg.type);
        if(listener != null)
            listener.accept(reg);
    }

    public static Future<ExerciseType<?>> ask(String type) {
        ExerciseTypeManager manager = Main.getExerciseManager();
        ExerciseType<?> handler = manager.get(type);
        if(handler != null)
            return CompletableFuture.completedFuture(handler);
        else {
            Main.getConnection().sendExerciseTypeRequest(type);
            CompletableFuture<ExerciseType<?>> res = new CompletableFuture<>();

            listeners.put(type, res::complete);
            return res;
        }
    }

    /**
     * An optimized version of ask + waitAll
     * @param types the types that want to be converted
     * @param callback the callback to be executed
     */
    public static void getAllOrWait(List<String> types, Consumer<List<ExerciseType<?>>> callback) {
        ExerciseTypeManager manager = Main.getExerciseManager();
        ExerciseType<?>[] found = new ExerciseType[types.size()];
        int index = 0;

        Iterator<String> iterator = types.iterator();
        while (iterator.hasNext()) {
            final String str = iterator.next();
            found[index] = manager.get(str);
            if(found[index] == null) {
                //Sync failure, go async and wait
                List<Future<ExerciseType<?>>> handlers = new ArrayList<>();
                for(int i = 0; i < index; i++)
                    handlers.add(CompletableFuture.completedFuture(found[i]));
                handlers.add(ask(str));
                while(iterator.hasNext())
                    handlers.add(ask(iterator.next()));
                new ExerciseWaiter(handlers, callback).async();
                return;
            } else index++;
        }
        callback.accept(Arrays.asList(found));
    }

    public static void waitAll(List<Future<ExerciseType<?>>> handlers, Consumer<List<ExerciseType<?>>> callback) {
        ExerciseWaiter waiter = new ExerciseWaiter(handlers, callback);
        for(Future<ExerciseType<?>> f : handlers)
            if(!f.isDone()) {
                waiter.async();
                return;
            }
        waiter.sync();
    }

    public static class ExerciseWaiter {

        private final List<Future<ExerciseType<?>>> handlers;
        private final Consumer<List<ExerciseType<?>>> callback;

        public ExerciseWaiter(List<Future<ExerciseType<?>>> handlers, Consumer<List<ExerciseType<?>>> callback) {
            this.handlers = handlers;
            this.callback = callback;
        }

        public void async() {
            waiters.submit(this::sync);
        }

        public void sync() {
            ExerciseType<?>[] res = new ExerciseType[handlers.size()];
            int i = 0;
            for (Future<ExerciseType<?>> handler : handlers)
                try {
                    res[i++] = handler.get();
                } catch (Exception e) {
                    Main.getUI().error(e);
                    return;
                }
            callback.accept(Arrays.asList(res));
        }
    }
}

package xyz.upperlevel.verifier.client;

import xyz.upperlevel.verifier.proto.ExerciseData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ExerciseUtil {
    protected static final ExecutorService waiters = Executors.newSingleThreadExecutor();


    public boolean register(ExerciseHandler<?> handler) {
        return exercises.putIfAbsent(handler.type, handler) == null;
    }

    public boolean remove(ExerciseHandler<?> handler) {
        return exercises.remove(handler.type) != null;
    }

    public boolean remove(String type) {
        return exercises.remove(type) != null;
    }

    public boolean registerOverride(ExerciseHandler<?> handler) {
        return exercises.put(handler.type, handler) != null;
    }

    public ExerciseHandler<?> get(String type) {
        return exercises.get(type);
    }

    public Future<ExerciseHandler<?>> ask(String type) {
        ExerciseHandler<?> handler = exercises.get(type);
        if(handler != null)
            return CompletableFuture.completedFuture(handler);
        else {
            Main.getConnection().sendExerciseTypeRequest(type);
            CompletableFuture<ExerciseHandler<?>> res = new CompletableFuture<>();
            listeners.put(type, res::complete);
            return res;
        }
    }

    /**
     * An optimized version of ask + waitAll
     * @param types the types that want to be converted
     * @param callback the callback to be executed
     */
    public void getAllOrWait(List<String> types, Consumer<List<ExerciseHandler<?>>> callback) {
        ExerciseHandler<?>[] found = new ExerciseHandler[types.size()];
        int index = 0;

        Iterator<String> iterator = types.iterator();
        while (iterator.hasNext()) {
            final String str = iterator.next();
            found[index] = get(str);
            if(found[index] == null) {
                //Sync failure, go async and wait
                List<Future<ExerciseHandler<?>>> handlers = new ArrayList<>();
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

    public void register(String exName, byte[] data) {
        try {
            for(ExerciseHandler<?> handler : load(save(new File(EX_FOLDER, exName + ".jar"), data).getPath())) {
                Consumer<ExerciseHandler<?>> fut = listeners.remove(handler.type);
                fut.accept(handler);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Main.getUI().error(e);
        }
    }

    private List<ExerciseHandler<?>> load(String pathToJar) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        JarFile jarFile = new JarFile(pathToJar);
        Enumeration<JarEntry> e = jarFile.entries();
        List<ExerciseHandler<?>> res = new ArrayList<>();

        URL[] urls = {new URL("jar:file:" + pathToJar + "!/")};
        URLClassLoader cl = URLClassLoader.newInstance(urls);

        while (e.hasMoreElements()) {
            JarEntry je = e.nextElement();
            if (je.isDirectory() || !je.getName().endsWith(".class")) {
                continue;
            }
            // -6 because of .class
            String className = je.getName().substring(0, je.getName().length() - 6);
            className = className.replace('/', '.');
            Class c = cl.loadClass(className);
            if(ExerciseHandler.class.isAssignableFrom(c)) {
                ExerciseHandler<?> handler = (ExerciseHandler<?>) c.newInstance();
                register(handler);
                res.add(handler);
            }
        }
        return res;
    }

    private static File save(File file, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }
        return file;
    }


    public static void waitAll(List<Future<ExerciseHandler<?>>> handlers, Consumer<List<ExerciseHandler<?>>> callback) {
        ExerciseWaiter waiter = new ExerciseWaiter(handlers, callback);
        for(Future<ExerciseHandler<?>> f : handlers)
            if(!f.isDone()) {
                waiter.async();
                return;
            }
        waiter.sync();
    }

    public List<ExerciseData> ancodeAll(List<Exercise> exercises) {
        return exercises.stream().map(Exercise::getData).collect(Collectors.toList());
    }

    public static class ExerciseWaiter {

        private final List<Future<ExerciseHandler<?>>> handlers;
        private final Consumer<List<ExerciseHandler<?>>> callback;

        public ExerciseWaiter(List<Future<ExerciseHandler<?>>> handlers, Consumer<List<ExerciseHandler<?>>> callback) {
            this.handlers = handlers;
            this.callback = callback;
        }

        public void async() {
            waiters.submit(this::sync);
        }

        public void sync() {
            ExerciseHandler<?>[] res = new ExerciseHandler[handlers.size()];
            int i = 0;
            for (Future<ExerciseHandler<?>> handler : handlers)
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

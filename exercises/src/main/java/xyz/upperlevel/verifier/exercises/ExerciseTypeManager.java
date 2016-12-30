package xyz.upperlevel.verifier.exercises;

import xyz.upperlevel.verifier.exercises.def.MultipleChoiceExerciseHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ExerciseTypeManager {
    public static final String EX_FOLDER_NAME = "exercises";
    public static final File EX_FOLDER;

    protected Map<String, ExerciseType<?>> exercises = new HashMap<>();
    protected List<Consumer<ExerciseType<?>>> listeners = new ArrayList<>();

    static {
        EX_FOLDER = new File(EX_FOLDER_NAME);
        EX_FOLDER.mkdirs();
        assert EX_FOLDER.isDirectory();
    }


    public ExerciseTypeManager() {
        register(MultipleChoiceExerciseHandler.INSTANCE);
    }

    public void loadSaved() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        if(EX_FOLDER == null)
            throw new IllegalStateException("The exercises folder isn't initialized");

        File[] files = EX_FOLDER.listFiles();
        if(files == null)
            throw new IllegalStateException("The EX_FOLDER is a directory!");
        for (File file : files) {
            load(file.getPath());
        }
    }

    public boolean register(ExerciseType<?> handler) {
        return exercises.putIfAbsent(handler.type, handler) == null;
    }

    public boolean remove(ExerciseType<?> handler) {
        return exercises.remove(handler.type) != null;
    }

    public boolean remove(String type) {
        return exercises.remove(type) != null;
    }

    public boolean registerOverride(ExerciseType<?> handler) {
        return exercises.put(handler.type, handler) != null;
    }

    public ExerciseType<?> get(String type) {
        return exercises.get(type);
    }

    public Path getFile(ExerciseType<?> type) {
        CodeSource source = type.getClass().getProtectionDomain().getCodeSource();
        if(source == null) {
            System.err.println("[WARNING]Cannot determine the Soruce of \"" + type.getClass().getSimpleName() + "\"");
            return null;
        }
        try {
            return Paths.get(source.getLocation().toURI());
        } catch (URISyntaxException e) {
            System.err.println("[WARNING]Error getting jar of \"" + type.getClass().getSimpleName() + "\"");
            e.printStackTrace();
        }
        return null;
    }

    public void registerListener(Consumer<ExerciseType<?>> event) {
        listeners.add(event);
    }

    public void register(String exName, byte[] data) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        for (ExerciseType<?> handler : load(save(new File(EX_FOLDER, exName + ".jar"), data).getPath()))
            listeners.forEach(t -> t.accept(handler));
    }

    private List<ExerciseType<?>> load(String pathToJar) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        JarFile jarFile = new JarFile(pathToJar);
        Enumeration<JarEntry> e = jarFile.entries();
        List<ExerciseType<?>> res = new ArrayList<>();

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
            if(ExerciseType.class.isAssignableFrom(c)) {
                ExerciseType<?> handler = (ExerciseType<?>) c.newInstance();
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

    public Collection<ExerciseType<?>> get() {
        return exercises.values();
    }
}

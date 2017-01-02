package xyz.upperlevel.verifier.server.assignments;

import lombok.Getter;
import xyz.upperlevel.verifier.proto.ErrorType;
import xyz.upperlevel.verifier.server.Main;
import xyz.upperlevel.verifier.server.assignments.converters.AssignmentConverter;
import xyz.upperlevel.verifier.server.assignments.exceptions.AlreadyCommittedException;
import xyz.upperlevel.verifier.server.login.AuthData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class AssignmentManager {
    public static final File FOLDER = new File("assignments");

    private String save_path = "{class}/{user}/{id}".replace("/", File.separator);

    private final List<Consumer<AssignmentRequest>> listeners = new ArrayList<>();

    @Getter
    private AssignmentRequest current = null;

    static {
        if(!FOLDER.isDirectory() && !FOLDER.mkdirs())
            throw new IllegalStateException("Cannot create assignments folder!");
        if(!FOLDER.isDirectory())
            throw new IllegalStateException("assignments is not a fodler!");
    }

    public void terminate() {
        synchronized (listeners) {
            current = null;
        }
    }

    public void load(File file) throws IOException {
        if(current != null)
            throw new IllegalStateException("Assignment already hosted!");
        synchronized (listeners) {
            current = new AssignmentRequest(AssignmentConverter.$.load(file), file.getName().replaceFirst("[.][^.]+$", ""));
            listeners.forEach(c -> c.accept(current));
            listeners.clear();
        }
    }

    public boolean hasCurrentAssignment(AuthData auth) {
        return new File(FOLDER, getPath(auth)).isFile();
    }

    public void commit(AuthData data, AssignmentResponse assignment) throws AlreadyCommittedException {
        File file = new File(FOLDER, getPath(data.getClazz(), data.getUsername(), assignment.getId()));
        if(file.exists())
            throw new AlreadyCommittedException();
        try {
            AssignmentConverter.$.save(file, assignment.toYaml());
        } catch (IOException e) {
            Main.getUi().error(ErrorType.MISC, "Error committing assignmnet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addListener(Consumer<AssignmentRequest> run) {
        synchronized (listeners) {
            if(current != null)
                run.accept(current);
            else
                listeners.add(run);
        }
    }


    public String getPath(AuthData data) {
        return getPath(data.getClazz(), data.getUsername(), getCurrent().getId());
    }

    public String getPath(String clazz, Set<String> user, String ass_id) {
        return save_path
                .replace("{class}", clazz)
                .replace("{user}", String.join("_", user))
                .replace("{id}", ass_id);
    }
}

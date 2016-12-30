package xyz.upperlevel.verifier.server.assignments;

import lombok.Getter;
import xyz.upperlevel.verifier.proto.ErrorType;
import xyz.upperlevel.verifier.server.Main;
import xyz.upperlevel.verifier.server.assignments.converters.AssignmentConverter;
import xyz.upperlevel.verifier.server.assignments.exceptions.AlreadyCommittedException;
import xyz.upperlevel.verifier.server.login.AuthData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AssignmentManager {
    public static final File FOLDER = new File("assignments");

    private String save_path = "{class}/{user}/{id}".replace("/", File.separator);

    @Getter
    private Assignment current = null;

    static {
        if(!FOLDER.isDirectory() && !FOLDER.mkdirs())
            throw new IllegalStateException("Cannot create assignments folder!");
        if(!FOLDER.isDirectory())
            throw new IllegalStateException("assignments is not a fodler!");
    }

    public void terminate() {
        current = null;
    }

    public void load(File file) throws FileNotFoundException {
        if(current != null)
            throw new IllegalStateException("Assignment already hosted!");
        current = new Assignment(AssignmentConverter.$.load(file), file.getName().replaceFirst("[.][^.]+$", ""));
    }

    public boolean hasCurrentAssignment(AuthData auth) {
        return new File(FOLDER, getPath(auth)).isFile();
    }

    public void commit(AuthData data, Assignment assignment) throws AlreadyCommittedException {
        File file = new File(FOLDER, getPath(data.getClazz(), data.getUsername(), assignment));
        if(file.exists())
            throw new AlreadyCommittedException();
        try {
            AssignmentConverter.$.save(file, assignment.toYamlResponse());
        } catch (IOException e) {
            Main.getUi().error(ErrorType.MISC, "Error committing assignmnet: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public String getPath(AuthData data) {
        return getPath(data.getClazz(), data.getUsername(), getCurrent());
    }

    public String getPath(String clazz, String user, Assignment assignment) {
        return save_path
                .replace("{class}", clazz)
                .replace("{user}", user.replace(" ", "_"))
                .replace("{id}", assignment.getId());
    }
}

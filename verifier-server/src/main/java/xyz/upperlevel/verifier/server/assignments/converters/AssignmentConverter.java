package xyz.upperlevel.verifier.server.assignments.converters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public interface AssignmentConverter {
    public static AssignmentConverter $ = new YamlAssignmentConverter();

    public Map<String, Object> load(File file) throws FileNotFoundException;

    public void save(File file, Map<String, Object> data) throws IOException;
}

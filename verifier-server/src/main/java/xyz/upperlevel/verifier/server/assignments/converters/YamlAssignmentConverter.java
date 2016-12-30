package xyz.upperlevel.verifier.server.assignments.converters;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Map;

public class YamlAssignmentConverter implements AssignmentConverter {
    private final Yaml yaml = new Yaml();

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> load(File file) throws FileNotFoundException {
        return (Map<String, Object>) yaml.load(new FileReader(file));
    }

    @Override public void save(File file, Map<String, Object> data) throws IOException {
        yaml.dump(data, new FileWriter(file));
    }
}

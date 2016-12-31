package xyz.upperlevel.verifier.server.assignments.converters;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class YamlAssignmentConverter implements AssignmentConverter {
    private final Yaml yaml = new Yaml();

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> load(File file) throws IOException {
        try(FileReader reader = new FileReader(file)) {
            return (Map<String, Object>) yaml.load(reader);
        }
    }

    @Override public void save(File file, Map<String, Object> data) throws IOException {
        file.getParentFile().mkdirs();
        try(FileWriter writer = new FileWriter(file)) {
            yaml.dump(data, writer);
        }
    }
}

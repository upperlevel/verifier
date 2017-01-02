package xyz.upperlevel.verifier.server.assignments.converters;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import xyz.upperlevel.verifier.server.assignments.converters.fraction.FractionConstructor;
import xyz.upperlevel.verifier.server.assignments.converters.fraction.FractionRepresenter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

public class YamlAssignmentConverter implements AssignmentConverter {
    private final Yaml yaml = new Yaml(new FractionConstructor(), new FractionRepresenter());

    public YamlAssignmentConverter() {
        yaml.addImplicitResolver(new Tag("!fraction"), Pattern.compile("\\d+/\\d+|\\d+"), "0123456789");
    }

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

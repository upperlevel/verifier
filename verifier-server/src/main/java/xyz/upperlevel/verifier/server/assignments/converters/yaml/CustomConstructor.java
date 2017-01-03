package xyz.upperlevel.verifier.server.assignments.converters.yaml;

import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import xyz.upperlevel.verifier.exercises.util.Fraction;

import java.time.LocalTime;

public class CustomConstructor extends Constructor{
    public CustomConstructor() {
        yamlConstructors.put(new Tag("!fraction"), new FractionConstruct());
        yamlConstructors.put(new Tag("!localtime"), new LocalTimeConstruct());
    }

    private class FractionConstruct extends AbstractConstruct {
        @Override
        public Object construct(Node node) {
            String str = (String) constructScalar((ScalarNode) node);
            int spl = str.indexOf('/');
            if(spl > 0)
                return new Fraction(
                        Integer.parseInt(str.substring(0, spl)),
                        Integer.parseInt(str.substring(spl + 1))
                );
            else
                return new Fraction(Integer.parseInt(str));
        }
    }

    private class LocalTimeConstruct extends AbstractConstruct {
        @Override
        public Object construct(Node node) {
            String str = (String) constructScalar((ScalarNode) node);
            return LocalTime.parse(str);
        }
    }
}

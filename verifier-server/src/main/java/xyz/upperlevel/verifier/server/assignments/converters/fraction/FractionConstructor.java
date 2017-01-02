package xyz.upperlevel.verifier.server.assignments.converters.fraction;

import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import xyz.upperlevel.verifier.exercises.util.Fraction;

public class FractionConstructor extends Constructor{
    public FractionConstructor() {
        yamlConstructors.put(new Tag("!fraction"), new FractionConstruct());
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
}

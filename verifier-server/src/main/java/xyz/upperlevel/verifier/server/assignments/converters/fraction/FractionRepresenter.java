package xyz.upperlevel.verifier.server.assignments.converters.fraction;

import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;
import xyz.upperlevel.verifier.exercises.util.Fraction;

public class FractionRepresenter extends Representer {
    public FractionRepresenter() {
        representers.put(Fraction.class, new RepresentFraction());
    }

    public class RepresentFraction implements Represent {
        @Override
        public Node representData(Object data) {
            Fraction fraction = (Fraction)data;
            String value = fraction.den == 0 ? (fraction.num + "") : (fraction.num + "/" + fraction.den);
            return representScalar(new Tag("!fraction"), value);
        }
    }
}

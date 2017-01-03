package xyz.upperlevel.verifier.server.assignments.converters.yaml;

import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;
import xyz.upperlevel.verifier.exercises.util.Fraction;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CustomRepresenter extends Representer {
    public CustomRepresenter() {
        representers.put(Fraction.class, new RepresentFraction());
        representers.put(LocalTime.class, new RepresentLocalTime());
    }

    public class RepresentFraction implements Represent {
        @Override
        public Node representData(Object data) {
            Fraction fraction = (Fraction)data;
            String value = fraction.den == 0 ? (fraction.num + "") : (fraction.num + "/" + fraction.den);
            return representScalar(new Tag("!fraction"), value);
        }
    }

    public class RepresentLocalTime implements Represent {
        @Override
        public Node representData(Object data) {
            LocalTime time = (LocalTime)data;
            String value = time.format(DateTimeFormatter.ISO_LOCAL_TIME);
            return representScalar(new Tag("!localtime"), value);
        }
    }
}

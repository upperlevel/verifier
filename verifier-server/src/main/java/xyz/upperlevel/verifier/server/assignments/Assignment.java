package xyz.upperlevel.verifier.server.assignments;

import lombok.Getter;
import xyz.upperlevel.verifier.exercises.Exercise;
import xyz.upperlevel.verifier.exercises.ExerciseType;
import xyz.upperlevel.verifier.exercises.ExerciseTypeManager;
import xyz.upperlevel.verifier.proto.AssignmentPacket;
import xyz.upperlevel.verifier.server.Main;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Assignment {
    @Getter
    private final List<Exercise<?>> exercises;
    @Getter
    private final AssignmentPacket packet;

    @Getter
    private final String id;

    public Assignment(List<Exercise<?>> exercises, String id) {
        this.exercises = exercises;
        packet = new AssignmentPacket(
                id,
                exercises.stream()
                        .map(Exercise::encodeRequest)
                        .collect(Collectors.toList())
        );
        this.id = id;
    }

    public Assignment(Map<String, Object> load, String id) {
        this(parse(load), id);
    }

    public Assignment(AssignmentPacket packet) {
        this.id = packet.getId();
        this.exercises = packet.getExercises()
                .stream()
                .map( t -> (Exercise<?>)Main.getExerciseTypeManager().get(t.getType()).decodeResponse(t.getData()))
                .collect(Collectors.toList());
        this.packet = packet;

    }

    @SuppressWarnings("unchecked")
    public static List<Exercise<?>> parse(Map<String, Object> map) {
        List<Exercise<?>> res = new ArrayList<>(128);
        final ExerciseTypeManager manager = Main.getExerciseTypeManager();

        if(map == null)
            throw new IllegalArgumentException("The loaded assignment doesn't have any value!");

        List<Map<String, Object>>  exercises = (List<Map<String, Object>>) map.get("exercises");

        if(exercises == null)
            throw new IllegalArgumentException("The loaded assignment doesn't have the \"exercises\" field (for the exercise list)");

        for(Map<String, Object> ex_raw : exercises) {
            final String type = (String) ex_raw.get("type");
            if(type == null)
                throw new IllegalArgumentException("The loaded assignment doesn't have a \"type\" field (for the exercise type)");

            final Map<String, Object> data = (Map<String, Object>) ex_raw.get("data");
            if(data == null)
                throw new IllegalArgumentException("The loaded assignment doesn't have a \"data\" field (for the exercise's data)");

            ExerciseType<?> ex_type = manager.get(type);

            if(ex_type == null)
                throw new IllegalArgumentException("Type not registered: \"" + type + "\"");

            res.add(ex_type.fromYamlRequest(data));
        }
        return res;
    }

    public Map<String, Object> toYaml(Function<Exercise<?>, Map<String, Object>> transformer) {
        return Collections.singletonMap(
                "exercises",
                exercises.stream()
                        .map(exe -> {
                            HashMap<String, Object> map = new HashMap<>(2);
                            map.put("type", exe.getType().type);
                            map.put("data", transformer.apply(exe));
                            return map;
                        })
                        .collect(Collectors.toList())
        );
    }

    public Map<String, Object> toYamlResponse() {
        return toYaml(Exercise::toYamlResponse);
    }

    public Map<String, Object> toYamlRequest() {
        return toYaml(Exercise::toYamlRequest);
    }
}

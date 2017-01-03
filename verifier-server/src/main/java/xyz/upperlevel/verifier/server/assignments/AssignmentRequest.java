package xyz.upperlevel.verifier.server.assignments;

import lombok.Getter;
import xyz.upperlevel.verifier.exercises.ExerciseRequest;
import xyz.upperlevel.verifier.exercises.ExerciseType;
import xyz.upperlevel.verifier.exercises.ExerciseTypeManager;
import xyz.upperlevel.verifier.proto.AssignmentPacket;
import xyz.upperlevel.verifier.proto.ExerciseData;
import xyz.upperlevel.verifier.server.Main;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AssignmentRequest {
    @Getter
    private final List<ExerciseRequest<?, ?>> exercises;

    @Getter
    private final String id;

    @Getter
    private LocalTime endTime = null;

    public AssignmentRequest(List<ExerciseRequest<?, ?>> exercises, String id) {
        this.exercises = exercises;
        this.id = id;
        this.endTime = endTime;
    }

    public AssignmentPacket getPacket(Random random) {
        int[] mapping = IntStream.range(0, exercises.size()).toArray();
        Collections.shuffle(Arrays.asList(mapping), random);

        AssignmentPacket packet = new AssignmentPacket(
                id,
                exercises.stream()
                        .map((exercise) -> exercise.encode(random))
                        .collect(Collectors.toList())
        );
        {//map-based shuffle
            List<ExerciseData> in = packet.getExercises();
            ExerciseData[] exes = new ExerciseData[in.size()];
            for(int i = 0; i < exes.length; i++)
                exes[mapping[i]] = in.get(i);
            packet.setExercises(Arrays.asList(exes));
        }
        return packet;
    }

    public AssignmentRequest(Map<String, Object> load, String id) {
        this(parse(load), id);
    }

    public void setTime(LocalTime time) {
        TimeSyncUtil.setTime(time);
    }

    public void setTimeUnsafe(LocalTime time) {
        this.endTime = time;
    }

    @SuppressWarnings("unchecked")
    public static List<ExerciseRequest<?, ?>> parse(Map<String, Object> map) {
        List<ExerciseRequest<?, ?>> res = new ArrayList<>(128);
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

            ExerciseType<?, ?> ex_type = manager.get(type);

            if(ex_type == null)
                throw new IllegalArgumentException("Type not registered: \"" + type + "\"");

            res.add(ex_type.fromYamlRequest(data));
        }
        return res;
    }

    public Map<String, Object> toYaml() {
        return Collections.singletonMap(
                "exercises",
                exercises.stream()
                        .map(exe -> {
                            HashMap<String, Object> map = new HashMap<>(2);
                            map.put("type", exe.getType().type);
                            map.put("data", exe.toYaml());
                            return map;
                        })
                        .collect(Collectors.toList())
        );
    }
}

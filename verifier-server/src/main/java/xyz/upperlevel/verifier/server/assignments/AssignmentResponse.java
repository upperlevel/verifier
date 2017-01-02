package xyz.upperlevel.verifier.server.assignments;

import lombok.Getter;
import xyz.upperlevel.verifier.exercises.ExerciseRequest;
import xyz.upperlevel.verifier.exercises.ExerciseResponse;
import xyz.upperlevel.verifier.exercises.ExerciseType;
import xyz.upperlevel.verifier.exercises.ExerciseTypeManager;
import xyz.upperlevel.verifier.proto.AssignmentPacket;
import xyz.upperlevel.verifier.proto.ExerciseData;
import xyz.upperlevel.verifier.server.Main;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AssignmentResponse {
    @Getter
    private final List<ExerciseResponse<?, ?>> exercises;

    @Getter
    private final String id;

    public AssignmentResponse(List<ExerciseResponse<?, ?>> exercises, String id) {
        this.exercises = exercises;
        this.id = id;
    }

    @SuppressWarnings("unchecked")
    public AssignmentResponse(AssignmentPacket packet, AssignmentRequest req, Random random) {
        this.id = packet.getId();

        int[] mapping = IntStream.range(0, packet.getExercises().size()).toArray();
        Collections.shuffle(Arrays.asList(mapping), random);

        ExerciseData[] exes = new ExerciseData[mapping.length];

        {//demap
            for(int i = 0; i < exes.length; i++)
                exes[mapping[i]] = packet.getExercises().get(i);
        }

        ExerciseResponse<?, ?>[] decoded = new ExerciseResponse[exes.length];
        List<ExerciseRequest<?, ?>> exercises = req.getExercises();
        {
            ExerciseTypeManager manager = Main.getExerciseTypeManager();
            for (int i = 0; i < exes.length; i++)
                decoded[i] = ((ExerciseType)manager.get(exes[i].getType())).decodeResponse(exes[i].getData(), exercises.get(i), random);
        }

        this.exercises = Arrays.asList(decoded);
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

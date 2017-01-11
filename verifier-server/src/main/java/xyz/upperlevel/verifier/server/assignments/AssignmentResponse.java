package xyz.upperlevel.verifier.server.assignments;

import com.google.protobuf.Timestamp;
import lombok.Getter;
import xyz.upperlevel.verifier.exercises.ExerciseRequest;
import xyz.upperlevel.verifier.exercises.ExerciseResponse;
import xyz.upperlevel.verifier.exercises.ExerciseType;
import xyz.upperlevel.verifier.exercises.ExerciseTypeManager;
import xyz.upperlevel.verifier.exercises.util.Fraction;
import xyz.upperlevel.verifier.proto.protobuf.AssignmentPacket;
import xyz.upperlevel.verifier.proto.protobuf.AssignmentPacket.ExerciseData;
import xyz.upperlevel.verifier.server.Main;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AssignmentResponse {
    @Getter
    private final List<ExerciseResponse<?, ?>> exercises;

    @Getter
    private final String id;

    private final Timestamp commitTime;
    private final Timestamp endTime;

    public AssignmentResponse(List<ExerciseResponse<?, ?>> exercises, String id, Timestamp commitTime, Timestamp endTime) {
        this.exercises = exercises;
        this.id = id;
        this.commitTime = commitTime;
        this.endTime = endTime;
    }

    @SuppressWarnings("unchecked")
    public AssignmentResponse(AssignmentPacket.Assignment packet, AssignmentRequest req, Random random) {
        this.id = packet.getId();
        this.commitTime = now();
        this.endTime = req.getEndTime();

        List<ExerciseData> in = packet.getDatasList();

        List<Integer> mapping = IntStream.range(0, in.size()).boxed().collect(Collectors.toList());
        Collections.shuffle(mapping, random);

        ExerciseData[] exes = new ExerciseData[mapping.size()];

        {//demap
            for(int i = 0; i < exes.length; i++)
                exes[mapping.get(i)] = in.get(i);
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

    private Timestamp now() {
        Instant now = Instant.now();
        return Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();
    }

    public Map<String, Object> toYaml() {
        final AtomicInteger points = new AtomicInteger(0);
        final AtomicInteger max = new AtomicInteger(0);

        HashMap<String, Object> map = new LinkedHashMap<>(4);
        map.put(
                "exercises",
                exercises.stream()
                        .map(exe -> {
                            HashMap<String, Object> ex = new HashMap<>(3);
                            ex.put("type", exe.getType().type);
                            ex.put("data", exe.toYaml());

                            Fraction res = exe.correct();
                            ex.put("res", res);
                            points.addAndGet(res.num);
                            max.addAndGet(res.den);
                            return ex;
                        })
                        .collect(Collectors.toList())
        );

        map.put("commit_time", commitTime);
        if(endTime != null)
            map.put("expire_time", endTime);
        map.put("result", new Fraction(points.get(), max.get()));

        return map;
    }
}

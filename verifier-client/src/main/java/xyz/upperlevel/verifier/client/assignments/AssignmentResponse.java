package xyz.upperlevel.verifier.client.assignments;

import lombok.AllArgsConstructor;
import lombok.Data;
import xyz.upperlevel.verifier.exercises.ExerciseResponse;

import java.util.List;

@AllArgsConstructor
@Data
public class AssignmentResponse {
    private final AssignmentRequest req;
    private final List<ExerciseResponse> exercises;

    public String getId() {
        return req.getId();
    }
}

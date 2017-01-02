package xyz.upperlevel.verifier.client.assignments;

import lombok.AllArgsConstructor;
import lombok.Data;
import xyz.upperlevel.verifier.exercises.ExerciseRequest;

import java.util.List;

@AllArgsConstructor
@Data
public class AssignmentRequest {
    private final String id;
    private final List<ExerciseRequest> exercises;
}

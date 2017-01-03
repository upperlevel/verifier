package xyz.upperlevel.verifier.client.assignments;

import lombok.Data;
import xyz.upperlevel.verifier.exercises.ExerciseRequest;

import java.time.LocalTime;
import java.util.List;
import java.util.function.Consumer;

@Data
public class AssignmentRequest {
    private final String id;
    private final List<ExerciseRequest> exercises;
    private LocalTime endTime;
    public Consumer<LocalTime> timeListener;

    public AssignmentRequest(String id, List<ExerciseRequest> exercises) {
        this.id = id;
        this.exercises = exercises;
    }

    public void setEndTime(LocalTime end) {
        this.endTime = end;
        if(timeListener != null)
            timeListener.accept(end);
        else
            System.out.println("[WARN]No time listener found!");
    }
}

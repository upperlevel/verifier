package xyz.upperlevel.verifier.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import xyz.upperlevel.verifier.exercises.Exercise;

import java.util.List;

@AllArgsConstructor
@Data
public class Assignment {
    private final String id;
    private final List<Exercise> exercises;
}

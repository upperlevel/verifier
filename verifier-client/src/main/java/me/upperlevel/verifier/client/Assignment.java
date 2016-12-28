package me.upperlevel.verifier.client;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class Assignment {
    private final List<Exercise> exercises;
}

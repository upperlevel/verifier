package me.upperlevel.verifier.client;

import lombok.AllArgsConstructor;

import java.util.IllegalFormatException;

@AllArgsConstructor
public abstract class ExerciseHandler<E extends Exercise> {
    public final String type;

    public abstract E decode(byte[] encoded) throws IllegalFormatException;

    public abstract byte[] encode(E exercise);
}

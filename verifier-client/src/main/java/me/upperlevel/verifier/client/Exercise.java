package me.upperlevel.verifier.client;

import javafx.scene.Parent;
import lombok.Getter;
import me.upperlevel.verifier.packetlib.PacketHandler;
import me.upperlevel.verifier.proto.ExerciseData;

public abstract class Exercise {
    @Getter
    private final PacketHandler handler;

    protected Exercise(PacketHandler handler) {
        this.handler = handler;
    }

    @SuppressWarnings("unchecked")
    public ExerciseData getData() {
        return new ExerciseData(handler.getName(), handler.encode(this));
    }

    public abstract Parent getGraphics();
}

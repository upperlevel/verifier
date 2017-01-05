package xyz.upperlevel.verifier.client.gui;

import javafx.scene.paint.Paint;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ColoredText {
    public static final ColoredText EMPTY = new ColoredText("", null);
    public final String text;
    public final Paint color;
}

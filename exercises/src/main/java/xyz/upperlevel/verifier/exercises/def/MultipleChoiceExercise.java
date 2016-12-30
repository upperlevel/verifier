package xyz.upperlevel.verifier.exercises.def;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import lombok.EqualsAndHashCode;
import xyz.upperlevel.verifier.exercises.Exercise;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(of={"multiple", "question", "choices"}, callSuper = false)
public class MultipleChoiceExercise extends Exercise<MultipleChoiceExercise> {

    public Boolean multiple;
    public String question;
    public List<String> choices;

    public Set<Integer> answers = Collections.emptySet();

    private volatile Parent graphics = null;

    public MultipleChoiceExercise(MultipleChoiceExerciseHandler handler) {
        super(handler);
    }


    @Override
    public Parent getGraphics() {
        if(graphics == null)
            graphics = createGraphics();
        return graphics;
    }

    public Parent createGraphics() {
        BorderPane parent = new BorderPane();
        Label question_field = new Label(question);
        parent.setTop(question_field);
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        if(multiple) {
            answers = new HashSet<>(choices.size());
            for(int i = 0; i < choices.size(); i++) {
                final int choice_num = i;
                CheckBox choice = new CheckBox(choices.get(i));
                choice.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue)
                        answers.add(choice_num);
                    else
                        answers.remove(choice_num);
                });
                box.getChildren().add(choice);
            }
        } else {
            ToggleGroup group = new ToggleGroup();
            for (int i = 0; i < choices.size(); i++) {
                RadioButton choice = new RadioButton(choices.get(i));
                choice.setToggleGroup(group);
                final int choice_num = i;
                choice.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue)
                        answers = Collections.singleton(choice_num);

                });
                box.getChildren().add(choice);
            }
        }
        parent.setCenter(box);

        return parent;
    }

    @Override
    public String toString() {
        return question;
    }
}

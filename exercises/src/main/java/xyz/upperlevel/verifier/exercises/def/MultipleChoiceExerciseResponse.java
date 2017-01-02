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
import xyz.upperlevel.verifier.exercises.ExerciseResponse;
import xyz.upperlevel.verifier.exercises.util.Fraction;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(of = "answers", callSuper = false)
public class MultipleChoiceExerciseResponse extends ExerciseResponse<MultipleChoiceExerciseRequest, MultipleChoiceExerciseResponse> {

    public Set<Integer> answers = Collections.emptySet();

    private Parent graphics = null;

    public MultipleChoiceExerciseResponse(MultipleChoiceExerciseHandler type, MultipleChoiceExerciseRequest req) {
        super(type, req);
    }

    @Override
    public Parent getGraphics() {
        if(graphics == null)
            graphics = createGraphics();
        return graphics;
    }

    @Override
    public Fraction correct() {
        Set<Integer> correct = getParent().answers;

        int corr_count = (int) answers.stream().filter(correct::contains).count();
        int wrong_count = answers.size() - corr_count;

        return new Fraction(corr_count - wrong_count, correct.size());
    }

    public Parent createGraphics() {
        final MultipleChoiceExerciseRequest req = getParent();

        BorderPane parent = new BorderPane();

        Label question_field = new Label(req.question);
        parent.setTop(question_field);
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        if(req.multiple) {
            answers = new HashSet<>(req.choices.size());
            for(int i = 0; i < req.choices.size(); i++) {
                final int choice_num = i;
                CheckBox choice = new CheckBox(req.choices.get(i));
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
            for (int i = 0; i < req.choices.size(); i++) {
                RadioButton choice = new RadioButton(req.choices.get(i));
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
}

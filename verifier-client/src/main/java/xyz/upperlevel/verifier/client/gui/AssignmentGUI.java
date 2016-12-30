package xyz.upperlevel.verifier.client.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import xyz.upperlevel.verifier.client.Assignment;
import xyz.upperlevel.verifier.client.Main;

import java.net.URL;

public class AssignmentGUI extends Application {
    private static AssignmentGUI instance = null;
    public static final String FXML_PATH = "gui/test-executor.fxml";
    private static final URL fxml = AssignmentGUI.class.getClassLoader().getResource(FXML_PATH);
    private static final String TITLE = "Assignment";

    static {
        if(fxml == null)
            throw new IllegalStateException("Cannot load \"" + fxml + "\"");
        else
            System.out.println("test-executor = Loaded succesfully");
    }

    @Getter
    private Stage stage;
    @Getter
    private AssignmentGUIController controller;
    @Getter
    private Scene scene;

    static {

    }

    public AssignmentGUI() {
        if(instance == null)
            instance = this;
        else
            throw new IllegalStateException("Instance already created");
    }

    public static AssignmentGUI getInstance() {
        return instance;
    }

    public void show(Assignment assignment) {
        controller.init(assignment.getId(), assignment.getExercises());
        stage.show();
    }


    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        URL fxml = getClass().getClassLoader().getResource(FXML_PATH);

        if(fxml == null)
            throw new IllegalStateException("Cannot find Assignment GUI (" + FXML_PATH + ")");

        FXMLLoader loader = new FXMLLoader(fxml);
        loader.setController(this.controller = new AssignmentGUIController());
        Parent root = loader.load(fxml.openStream());
        scene = new Scene(root);
        stage.setOnCloseRequest(event -> Main.shutdown());
        stage.setScene(scene);
        stage.setTitle(TITLE);
    }
}

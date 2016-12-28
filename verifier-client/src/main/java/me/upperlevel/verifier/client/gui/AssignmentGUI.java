package me.upperlevel.verifier.client.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;

import java.net.URL;

public class AssignmentGUI extends Application {
    private static AssignmentGUI instance = null;
    private static final String FXML_PATH = "gui/test-executor.fxml";
    private static final String TITLE = "Assignment";

    @Getter
    private Stage stage;
    @Getter
    private AssignmentGUIController controller;
    @Getter
    private Scene scene;

    public AssignmentGUI() {
        if(instance == null)
            instance = this;
        else
            throw new IllegalStateException("Instance already created");
    }

    public static AssignmentGUI getInstance() {
        return instance;
    }


    @Override public void start(Stage stage) throws Exception {
        this.stage = stage;
        URL fxml = getClass().getClassLoader().getResource(FXML_PATH);

        if(fxml == null)
            throw new IllegalStateException("Cannot find Assignment GUI (" + FXML_PATH + ")");

        FXMLLoader loader = new FXMLLoader(fxml);
        loader.setController(this.controller = new AssignmentGUIController());
        Parent root = loader.load(fxml.openStream());
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(TITLE);
    }
}

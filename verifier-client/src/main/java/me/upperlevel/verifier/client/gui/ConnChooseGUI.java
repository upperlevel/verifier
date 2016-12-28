package me.upperlevel.verifier.client.gui;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

public class ConnChooseGUI implements Initializable{
    private static final String FXML_PATH = "gui/conn-chosser.fxml";
    private static final String TITLE = "Choose Ip:Port";

    private BiConsumer<String, Integer> callback;
    private Parent root;
    private Stage stage;
    private Scene scene;

    private TextField host_field;
    private TextField port_field;

    public void show(BiConsumer<String, Integer> callback) {
        this.callback = callback;
        if(stage == null)
            start();
        stage.show();
    }

    public void onExit() {
        stage.hide();
        callback.accept(null, null);
    }

    public void onConnect() {
        stage.hide();
        callback.accept(host_field.getText(), Integer.parseUnsignedInt(port_field.getText()));
    }


    public void start() {
        URL fxml = getClass().getClassLoader().getResource(FXML_PATH);
        FXMLLoader loader = new FXMLLoader(fxml);
        loader.setController(this);
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Cannot load The connection chooser fxml (" + FXML_PATH + ")");
        }
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initOwner(AssignmentGUI.getInstance().getStage().getOwner());
        stage.setAlwaysOnTop(true);
        stage.setTitle(TITLE);
        stage.setScene(this.scene = new Scene(root));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        port_field.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if(!newValue.matches("\\d"))
                        port_field.setText(newValue.replaceAll("[^\\d]", ""));
                }
        );
    }
}

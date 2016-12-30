package xyz.upperlevel.verifier.client.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import xyz.upperlevel.verifier.client.Main;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

public class ConnChooseGUI implements Initializable{
    public static final String FXML_PATH = "gui/conn-chooser.fxml";
    public static final URL fxml = ConnChooseGUI.class.getClassLoader().getResource(FXML_PATH);
    private static final String TITLE = "Choose Ip:Port";

    private BiConsumer<String, Integer> callback;
    private Parent root;
    private Stage stage;
    private Scene scene;

    @FXML
    private TextField host_field;
    @FXML
    private TextField port_field;

    static {
        if(fxml == null)
            throw new IllegalStateException("Cannot find file \"" + FXML_PATH + "\"");
    }

    public void show(BiConsumer<String, Integer> callback) {
        this.callback = callback;
        if(stage == null)
            start();
        stage.show();
    }

    @FXML
    public void onExit() {
        stage.hide();
        callback.accept(null, null);
    }

    @FXML
    public void onConnect() {
        stage.hide();
        callback.accept(host_field.getText(), Integer.parseUnsignedInt(port_field.getText()));
    }


    public void start() {
        FXMLLoader loader = new FXMLLoader(fxml);
        loader.setController(this);
        try {
            root = loader.load(fxml.openStream());
        } catch (IOException e) {
            throw new RuntimeException("Cannot load The connection chooser fxml (" + FXML_PATH + ")", e);
        }
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setOnCloseRequest(event -> Main.shutdown());
        //stage.initStyle(StageStyle.UNDECORATED);
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

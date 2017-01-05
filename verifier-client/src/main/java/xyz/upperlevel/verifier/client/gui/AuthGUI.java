package xyz.upperlevel.verifier.client.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import xyz.upperlevel.verifier.client.AuthToken;
import xyz.upperlevel.verifier.client.Main;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.function.Consumer;

public class AuthGUI {
    private static final String FXML_PATH = "gui/auth.fxml";
    private static final String TITLE = "Auth";

    private Consumer<AuthToken> callback;
    private Parent root;
    private Stage stage;
    private Scene scene;

    @FXML
    private TextField class_field;
    @FXML
    private TextField user_field;
    @FXML
    private PasswordField password_field;

    public void show(Consumer<AuthToken> callback) {
        this.callback = Objects.requireNonNull(callback);
        if(stage == null)
            start();
        stage.show();
    }

    @FXML
    public void onExit() {
        stage.hide();
        callback.accept(null);
        password_field.setText("");
    }

    @FXML
    public void onConfirm() {
        stage.hide();
        callback.accept(new AuthToken(class_field.getText(), user_field.getText(), password_field.getText()));
        password_field.setText("");
    }


    public void start() {
        URL fxml = getClass().getClassLoader().getResource(FXML_PATH);
        if(fxml == null)
            throw new IllegalStateException("Cannot find \"" + FXML_PATH + "\"!");
        FXMLLoader loader = new FXMLLoader(fxml);
        loader.setController(this);
        try {
            root = loader.load(fxml.openStream());
        } catch (IOException e) {
            throw new RuntimeException("Cannot load The connection chooser fxml (" + FXML_PATH + ")", e);
        }
        stage = new Stage();
        stage.setOnCloseRequest(event -> {
            stage.hide();
            Main.shutdown();
        });
        stage.initModality(Modality.APPLICATION_MODAL);
        //stage.initStyle(StageStyle.UNDECORATED);
        stage.initOwner(AssignmentGUI.getInstance().getStage().getOwner());
        stage.setAlwaysOnTop(true);
        stage.setTitle(TITLE);
        stage.setScene(this.scene = new Scene(root));
    }
}

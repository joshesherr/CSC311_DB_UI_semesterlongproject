package viewmodel;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import service.UserSession;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;


public class LoginController implements Initializable {


    public Button themeToggle;
    @FXML
    public BorderPane root;
    public TextField usernameTextField;
    public PasswordField passwordField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        usernameTextField.setText(Preferences.userRoot().get("USERNAME",""));
        passwordField.setText(Preferences.userRoot().get("PASSWORD",""));

        if (Preferences.userRoot().get("THEME","/css/lightTheme.css").equals("/css/lightTheme.css")) {
            themeToggle.setText("\uD83C\uDF19");
        } else {
            themeToggle.setText("☀");
        }
        themeToggle.setOnAction(e->{
            if (Preferences.userRoot().get("THEME","/css/lightTheme.css").equals("/css/lightTheme.css")) {
                themeToggle.setText("☀");
                Preferences.userRoot().put("THEME","/css/darkTheme.css");
            } else {
                themeToggle.setText("\uD83C\uDF19");
                Preferences.userRoot().put("THEME","/css/lightTheme.css");
            }
            root.getScene().getStylesheets().clear();
            root.getScene().getStylesheets().add(getClass().getResource(Preferences.userRoot().get("THEME","/css/lightTheme.css")).toExternalForm());
        });
    }
    private static BackgroundImage createImage(String url) {
        return new BackgroundImage(
                new Image(url),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
                new BackgroundPosition(Side.LEFT, 0, true, Side.BOTTOM, 0, true),
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, false, true));
    }
    @FXML
    public void login(ActionEvent actionEvent) {

        UserSession.getInstance(usernameTextField.getText(), passwordField.getText());

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/db_interface_gui.fxml"));
            Scene scene = new Scene(root, 960, 600);
            scene.getStylesheets().add(getClass().getResource(Preferences.userRoot().get("THEME","/css/lightTheme.css")).toExternalForm());
            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void signUp(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/signUp.fxml"));
            Scene scene = new Scene(root, 960, 600);
            scene.getStylesheets().add(getClass().getResource(Preferences.userRoot().get("THEME","/css/lightTheme.css")).toExternalForm());
            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

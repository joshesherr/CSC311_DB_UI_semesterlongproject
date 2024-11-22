package viewmodel;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class SignUpController implements Initializable {

    public BorderPane root;
    public Button themeToggle;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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

    public void createNewAccount(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ran into a problem...");
        alert.setContentText("We are having trouble connecting to our servers\nPlease try again later.");
        alert.showAndWait();
    }

    public void goBack(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
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

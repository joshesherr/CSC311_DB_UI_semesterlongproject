package viewmodel;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

public class HTMLViewerController implements Initializable {

    @FXML
    WebView webView;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void showPage(int type) {
        switch (type) {
            case 0: webView.getEngine().load(getClass().getResource("/view/about.html").toString());
            break;
            case 1: webView.getEngine().load(getClass().getResource("/view/help.html").toString());
            break;
        }
    }
}

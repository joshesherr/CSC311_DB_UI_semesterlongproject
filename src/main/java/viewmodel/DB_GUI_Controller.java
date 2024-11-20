package viewmodel;

import com.azure.storage.blob.BlobClient;
import dao.DbConnectivityClass;
import dao.StorageUploader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Major;
import model.Person;
import service.MyLogger;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;

public class DB_GUI_Controller implements Initializable {

    StorageUploader store = new StorageUploader();
    public ProgressIndicator progressBar;
    @FXML
    Button addBtn, editBtn, deleteBtn;
    @FXML
    TextField first_name, last_name, department, email, imageURL;
    @FXML
    ComboBox<String> major;
    @FXML
    ImageView img_view;
    @FXML
    MenuBar menuBar;
    @FXML
    private TableView<Person> tv;
    @FXML
    private TableColumn<Person, Integer> tv_id;
    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;
    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();

    /*
    ToDo 1. Disable the "Edit" and "Delete" button unless a record is selected from the table view.
    Todo 2. The "Add" button should be enabled only if the form fields contain correct information.
    Todo 3. Menu items corresponding to actions should be grayed out if the required selection is not made.
     */

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        ObservableList<String> li = FXCollections.observableArrayList();
        for (Major v :  Major.values()) li.add(v.getMajorName());
        major.setItems(li);

    }

    @FXML
    protected void addNewRecord() {
            Person p = new Person(first_name.getText(), last_name.getText(), department.getText(),
                    Major.values()[major.getSelectionModel().getSelectedIndex()], email.getText(), imageURL.getText());
            cnUtil.insertUser(p);
            p.setId(cnUtil.retrieveId(p));
            data.add(p);
            clearForm();
    }

    @FXML
    protected void clearForm() {
        first_name.setText("");
        last_name.setText("");
        department.setText("");
        major.setValue(null);
        email.setText("");
        imageURL.setText("");
        addBtn.setDisable(true);
        editBtn.setDisable(true);
        deleteBtn.setDisable(true);
    }

    @FXML
    protected void logOut(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").getFile());
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    @FXML
    protected void displayAbout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/about.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void editRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        Person p2 = new Person(index + 1, first_name.getText(), last_name.getText(), department.getText(),
                Major.values()[major.getSelectionModel().getSelectedIndex()], email.getText(),  imageURL.getText());
        cnUtil.editUser(p.getId(), p2);
        data.remove(p);
        data.add(index, p2);
        tv.getSelectionModel().select(index);
    }

    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        cnUtil.deleteRecord(p);
        data.remove(index);
        tv.getSelectionModel().select(index);
    }

    @FXML
    protected void showImage() {
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));
            Task<Void> uploadTask = createUploadTask(file, progressBar);
            progressBar.progressProperty().bind(uploadTask.progressProperty());
            new Thread(uploadTask).start();
        }
    }

    private Task<Void> createUploadTask(File file, ProgressIndicator progressBar) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                BlobClient blobClient = store.getContainerClient().getBlobClient(file.getName());
                long fileSize = Files.size(file.toPath());
                long uploadedBytes = 0;

                try (FileInputStream fileInputStream = new FileInputStream(file);
                     OutputStream blobOutputStream = blobClient.getBlockBlobClient().getBlobOutputStream()) {

                    byte[] buffer = new byte[1024 * 1024]; // 1 MB buffer size
                    int bytesRead;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        blobOutputStream.write(buffer, 0, bytesRead);
                        uploadedBytes += bytesRead;

                        // Calculate and update progress as a percentage
                        int progress = (int) ((double) uploadedBytes / fileSize * 100);
                        updateProgress(progress, 100);
                    }
                }
                return null;
            }
        };
    }


    @FXML
    protected void addRecord() {
        showSomeone();
    }

    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p==null) {
            editBtn.setDisable(true);
            deleteBtn.setDisable(true);
            return;
        }
        first_name.setText(p.getFirstName());
        last_name.setText(p.getLastName());
        department.setText(p.getDepartment());
        major.setValue(p.getMajor().getMajorName());
        email.setText(p.getEmail());
        imageURL.setText(p.getImageURL());
        formValidation();

        editBtn.setDisable(false);
        deleteBtn.setDisable(false);
    }

    public void lightTheme(ActionEvent actionEvent) {
        try {
            Scene scene = menuBar.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            System.out.println("light " + scene.getStylesheets());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void darkTheme(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) menuBar.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField textField1 = new TextField("Name");
        TextField textField2 = new TextField("Last Name");
        TextField textField3 = new TextField("Email ");
        ObservableList<Major> options =
                FXCollections.observableArrayList(Major.values());
        ComboBox<Major> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();
        dialogPane.setContent(new VBox(8, textField1, textField2,textField3, comboBox));
        Platform.runLater(textField1::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new Results(textField1.getText(),
                        textField2.getText(), comboBox.getValue());
            }
            return null;
        });
        Optional<Results> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((Results results) -> {
            MyLogger.makeLog(
                    results.fname + " " + results.lname + " " + results.major);
        });
    }

    private static final int
    FIRST_NAME=0,
    LAST_NAME=1,
    DEPARTMENT=2,
    MAJOR=3,
    EMAIL=4,
    IMAGE_URL=5;
    /**
     * @return A number whose bits represent which
     * elements in the form are invalid.
     */
    public int checkInputFields() {
        int i=0;
        i += inputValidation(first_name.getText(),FIRST_NAME)
                ?0:(int)Math.pow(2,FIRST_NAME);
        i += inputValidation(first_name.getText(),LAST_NAME)
                ?0:(int)Math.pow(2,LAST_NAME);
        i += inputValidation(department.getText(),DEPARTMENT)
                ?0:(int)Math.pow(2,DEPARTMENT);
        i += (major.getValue()!=null)
                ?0:(int)Math.pow(2,MAJOR);
        i += inputValidation(email.getText(),EMAIL)
                ?0:(int)Math.pow(2,EMAIL);
        i += inputValidation(imageURL.getText(),IMAGE_URL)
                ?0:(int)Math.pow(2,IMAGE_URL);
        return i;
    }

    /**
     *
     * @param s the string being validated.
     * @param validationType which validation method to use.
     * @return True or False depending on if the string passed the tests.
     */
    public boolean inputValidation(String s, int validationType) {
        return switch (validationType) {
            case FIRST_NAME, LAST_NAME -> s.matches("^[^ ±!@£$%^&*_+§¡€#¢§¶•ªº«\\\\/<>?:;|=.,]{1,20}$");
            case DEPARTMENT -> s.matches("^[^±!@£$%^&*_+§¡€#¢§¶•ªº«\\\\/<>?:;|=.,]{1,}$");
            case MAJOR -> {for (Major m : Major.values()) {if (m.equals(s)) yield true;}yield false;}
            case EMAIL -> s.matches("^(?!\\.)[\\w\\-_.]*[^.]@farmingdale.edu$");
            case IMAGE_URL -> s.matches("([^?#]*\\/)?([^?#]*\\.([Jj][Pp][Ee]?[Gg]|[Pp][Nn][Gg]|[Gg][Ii][Ff]))(?:\\?([^#]*))?(?:#(.*))?$");
            default -> false;
        };
    }

    private void formValidation() {
        int invalidBits=checkInputFields();
        //region example
//        if ( (invalidBits & (1<<FIRST_NAME) )!=0 ) System.out.println("First Name is invalid");
//        if ( (invalidBits & (1<<LAST_NAME) )!=0 ) System.out.println("Last Name is invalid");
//        if ( (invalidBits & (1<<DEPARTMENT) )!=0 ) System.out.println("Department Name is invalid");
//        if ( (invalidBits & (1<<MAJOR) )!=0 ) System.out.println("Major Name is invalid");
//        if ( (invalidBits & (1<<EMAIL) )!=0 ) System.out.println("Email is invalid");
//        if ( (invalidBits & (1<<IMAGE_URL) )!=0 ) System.out.println("Image is invalid");
        //endregion
        addBtn.setDisable(invalidBits!=0);// if any bits are set, disable the add and edit button
        editBtn.setDisable(invalidBits!=0);
    }

    @FXML
    protected void inputFieldUpdated(KeyEvent keyEvent) {
        formValidation();
    }

    //TODO and function to import and export csv
    public void importCSV(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export as CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Comma Separated List", "*.csv"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = fileChooser.showOpenDialog(tv.getScene().getWindow());

        if (file == null) return;

        try {
            Scanner csvReader = new Scanner(file);
            csvReader.next();
            csvReader.useDelimiter("\n");
            cnUtil.deleteAllRecords();
            data.clear();

            while (csvReader.hasNext()) {
                Scanner tokens = new Scanner(csvReader.next());
                tokens.useDelimiter(",");

                String firstName=tokens.next(), lastName=tokens.next(), department=tokens.next(), major=tokens.next(), email=tokens.next(), imageUrl=tokens.next();
                boolean valid = inputValidation(firstName, FIRST_NAME)
                        && inputValidation(lastName, LAST_NAME)
                        && inputValidation(department, DEPARTMENT)
                        && inputValidation(major, MAJOR)
                        && inputValidation(email, EMAIL)
                        && inputValidation(imageUrl, IMAGE_URL);

                if (!valid) throw new Exception("Invalid CSV Data.");

                Person p = new Person(firstName,lastName,department,Major.valueOf(major),email,imageUrl);
                cnUtil.insertUser(p);
                p.setId(cnUtil.retrieveId(p));
                data.add(p);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void exportCSV(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export as CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Comma Separated List", "*.csv"));
        fileChooser.setInitialFileName("User-Table.csv");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = fileChooser.showSaveDialog(tv.getScene().getWindow());

        if (file == null) return;

        try {
            FileWriter outputFile = new FileWriter(file.getPath());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("id,firstName,lastName,department,major,email,imageURL");
            for (Person p : data) {
                stringBuilder.append("\n").append(p.toCSVData());
            }
            outputFile.write(stringBuilder.toString());
            outputFile.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static class Results {
        String fname;
        String lname;
        Major major;

        public Results(String name, String date, Major venue) {
            this.fname = name;
            this.lname = date;
            this.major = venue;
        }
    }

}
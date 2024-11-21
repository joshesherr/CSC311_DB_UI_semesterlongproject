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
import model.Validator;
import service.MyLogger;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DB_GUI_Controller implements Initializable {

    public ProgressIndicator progressIndicator;
    public ProgressBar progressBar;
    @FXML
    Label errorText;
    StorageUploader store = new StorageUploader();
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
    private boolean formDisabled=false;

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

        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        ObservableList<String> li = FXCollections.observableArrayList();
        for (Major v :  Major.values()) li.add(v.getMajorName());
        major.setItems(li);

        major.getEditor().setOnKeyTyped(e->{
            inputFieldUpdated();
        });

        progressIndicator.progressProperty().bind(progressBar.progressProperty());
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
    protected void addNewRecord() {
        Person p = new Person(first_name.getText(), last_name.getText(), department.getText(), Major.values()[major.getSelectionModel().getSelectedIndex()], email.getText(), imageURL.getText());
        addNewRecord(p);
    }
    private void addNewRecord(Person p) {
        Task<Boolean> insertTask = cnUtil.insertUser(p);
        errorText.textProperty().bind(insertTask.messageProperty());

        insertTask.setOnRunning(s->{
            disableForm();
        });
        insertTask.setOnSucceeded(s->{
            p.setId(cnUtil.retrieveId(p));
            data.add(p);
            enableForm();
        });
        insertTask.setOnCancelled(s->enableForm());
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(insertTask); // start the task
        executorService.shutdown();
    }

    @FXML
    protected void clearForm() {
        first_name.setText("");
        last_name.setText("");
        department.setText("");
        major.getSelectionModel().clearSelection();
        email.setText("");
        imageURL.setText("");
        addBtn.setDisable(true);
        editBtn.setDisable(true);
        deleteBtn.setDisable(true);
        tv.getSelectionModel().clearSelection();
    }

    @FXML
    protected void editRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        Person p2 = new Person(index + 1, first_name.getText(), last_name.getText(), department.getText(),
                Major.values()[major.getSelectionModel().getSelectedIndex()], email.getText(),  imageURL.getText());

        Task<Boolean> editTask = cnUtil.editUser(p.getId(), p2);
        errorText.textProperty().bind(editTask.messageProperty());

        editTask.setOnRunning(s->{
            disableForm();
        });
        editTask.setOnSucceeded(s->{
            data.remove(p);
            data.add(index, p2);
            enableForm();
            tv.getSelectionModel().select(index);
        });
        editTask.setOnCancelled(s->enableForm());
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(editTask); // start the task
        executorService.shutdown();
    }

    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        tv.getSelectionModel().select(index);

        Task<Boolean> deleteTask = cnUtil.deleteRecord(p);
        errorText.textProperty().bind(deleteTask.messageProperty());

        deleteTask.setOnRunning(s->{
            disableForm();
        });
        deleteTask.setOnSucceeded(s->{
            data.remove(index);
            clearForm();
            enableForm();
        });
        deleteTask.setOnCancelled(s->enableForm());
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(deleteTask); // start the task
        executorService.shutdown();
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

    public void disableForm() {
        formDisabled = true;
        addBtn.setDisable(true);
        editBtn.setDisable(true);
        deleteBtn.setDisable(true);
        first_name.setDisable(true);
        last_name.setDisable(true);
        department.setDisable(true);
        email.setDisable(true);
        imageURL.setDisable(true);
        major.setDisable(true);
    }
    public void enableForm() {
        formDisabled=false;
        formValidation();
        first_name.setDisable(false);
        last_name.setDisable(false);
        department.setDisable(false);
        email.setDisable(false);
        imageURL.setDisable(false);
        major.setDisable(false);
    }

    @FXML
    protected void addRecord() {
        showSomeone();
    }

    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p==null) return;
        if (isFormDisabled()) return;
        first_name.setText(p.getFirstName());
        last_name.setText(p.getLastName());
        department.setText(p.getDepartment());
        major.setValue(p.getMajor().getMajorName());
        email.setText(p.getEmail());
        imageURL.setText(p.getImageURL());
        formValidation();
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

    private void formValidation() {
        int invalidBits = Validator.validate(first_name.getText(), Validator.FIRST_NAME)
            +  Validator.validate(first_name.getText(), Validator.LAST_NAME)
            +  Validator.validate(department.getText(), Validator.DEPARTMENT)
            +  Validator.validate(major.getEditor().getText(), Validator.MAJOR)
            +  Validator.validate(email.getText(), Validator.EMAIL)
            +  Validator.validate(imageURL.getText(), Validator.IMAGE_URL);
        addBtn.setDisable(invalidBits!=0);// if any bits are set, disable the add and edit button
        editBtn.setDisable(tv.selectionModelProperty().get().isEmpty());
        deleteBtn.setDisable(tv.selectionModelProperty().get().isEmpty());
    }

    @FXML
    protected void inputFieldUpdated() {
        formValidation();
    }

    //TODO Make this import on a different thread to avoid program freezing.
    public void importCSV() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export as CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Comma Separated List", "*.csv"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = fileChooser.showOpenDialog(tv.getScene().getWindow());

        if (file == null) return;

        try {
            Scanner csvReader = new Scanner(file);
            csvReader.nextLine();
            List<Person> persons = new ArrayList<>();
            int lineNumber = 0;
            while (csvReader.hasNext()) {
                String line = csvReader.nextLine();
                lineNumber++;
                Scanner tokens = new Scanner(line);
                tokens.useDelimiter(",");
                String id = tokens.hasNext()?tokens.next():"",
                        firstName=tokens.hasNext()?tokens.next():"",
                        lastName=tokens.hasNext()?tokens.next():"",
                        department=tokens.hasNext()?tokens.next():"",
                        major=tokens.hasNext()?tokens.next():"",
                        email=tokens.hasNext()?tokens.next():"",
                        imageUrl=tokens.hasNext()?tokens.next():"";
                int invalidBits =  Validator.validate(firstName, Validator.FIRST_NAME)
                        +  Validator.validate(lastName,  Validator.LAST_NAME)
                        +  Validator.validate(department,  Validator.DEPARTMENT)
                        +  Validator.validate(major,  Validator.MAJOR)
                        +  Validator.validate(email,  Validator.EMAIL)
                        +  Validator.validate(imageUrl,  Validator.IMAGE_URL);
                if (invalidBits!=0) {
                    errorText.setText("CSV Data is not valid. Invalid Info found in row "+lineNumber+". Check field(s): "+ Validator.invalidBitsToString(invalidBits));
                    MyLogger.makeLog("CSV Data is not valid.\n" + "Invalid Info: "+ Validator.invalidBitsToString(invalidBits));
                    return;
                }
                persons.add(new Person(firstName, lastName, department, Major.valueOf(major), email, imageUrl));
            }


            Task<Void> deleteAllTask = cnUtil.deleteAllRecords();
            deleteAllTask.setOnRunning(c->disableForm());
            deleteAllTask.setOnCancelled(s->enableForm());

            deleteAllTask.setOnSucceeded(s->{
                data.clear();

                Task<Boolean> insertAllTask = cnUtil.insertAllUser(persons);
                progressBar.progressProperty().bind(insertAllTask.progressProperty());
                errorText.textProperty().bind(insertAllTask.messageProperty());

                insertAllTask.setOnSucceeded(k-> {
                    enableForm();
                    cnUtil.getData();
                });
                insertAllTask.setOnCancelled(k->enableForm());

                ExecutorService executorService2 = Executors.newFixedThreadPool(1);
                executorService2.execute(insertAllTask);
                executorService2.shutdown();

            });
            ExecutorService executorService1 = Executors.newFixedThreadPool(1);
            executorService1.execute(deleteAllTask);
            executorService1.shutdown();
            errorText.textProperty().bind(deleteAllTask.messageProperty());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void exportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export as CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Comma Separated List", "*.csv"));
        fileChooser.setInitialFileName("User-Table.csv");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = fileChooser.showSaveDialog(tv.getScene().getWindow());

        if (file == null) return;

        Task<Void> exportCSVTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    updateMessage("Exporting...");
                    FileWriter outputFile = new FileWriter(file.getPath());
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("id,firstName,lastName,department,major,email,imageURL");
                    for (int i = 0; i < data.size(); i++) {
                        updateProgress(i+1, data.size());
                        stringBuilder.append("\n").append(data.get(i).toCSVData());
                    }
                    outputFile.write(stringBuilder.toString());
                    outputFile.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                updateProgress(1,1);
                updateMessage("Export Complete!");
                return null;
            }
        };

        progressBar.progressProperty().bind(exportCSVTask.progressProperty());
        errorText.textProperty().bind(exportCSVTask.messageProperty());

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(exportCSVTask);
        executorService.shutdown();
    }

    public boolean isFormDisabled() {
        return formDisabled;
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
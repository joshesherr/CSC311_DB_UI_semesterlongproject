module com.example.csc311_db_ui_semesterlongproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.azure.storage.blob;
    requires jdk.dynalink;
    requires javafx.web;
    requires java.desktop;
    requires java.prefs;

    opens viewmodel;
    exports viewmodel;
    opens dao;
    exports dao;
    opens model;
    exports model;
}
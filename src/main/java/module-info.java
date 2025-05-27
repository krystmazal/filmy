module com.example.filmy {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.sql;

    opens com.example.filmy to javafx.fxml;
    exports com.example.filmy;
}
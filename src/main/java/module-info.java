module com.example.spacefight {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.example.spacefight to javafx.fxml;
    exports com.example.spacefight;
}

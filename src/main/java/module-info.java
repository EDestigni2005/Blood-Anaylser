module com.example.bloodanalyser {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.bloodanalyser to javafx.fxml;
    exports com.example.bloodanalyser;
}
module org.example.syntak {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.logging;
    requires static lombok;


    opens org.example.syntak to javafx.fxml;
    exports org.example.syntak;
}
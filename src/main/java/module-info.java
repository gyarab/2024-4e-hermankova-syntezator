module org.example.synthesiser {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.gluonhq.scenebuilder.kit;
    requires java.desktop;


    opens org.example.synthesiser to javafx.fxml;
    exports org.example.synthesiser;
}
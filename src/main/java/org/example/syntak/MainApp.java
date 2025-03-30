package org.example.syntak;

import javafx.application.Application; // Import třídy Application z JavaFX
import javafx.fxml.FXMLLoader; // Import třídy FXMLLoader pro načítání FXML souborů
import javafx.scene.Scene; // Import třídy Scene pro vytvoření scény
import javafx.stage.Stage; // Import třídy Stage pro reprezentaci okna aplikace
import java.io.IOException; // Import třídy IOException pro zachycení výjimek při I/O operacích
import java.util.Objects; // Import třídy Objects pro práci s objekty

public class MainApp extends Application { // Hlavní třída aplikace, která dědí od třídy Application
    @Override
    public void start(Stage primaryStage) throws IOException { // Metoda start, která se volá při spuštění aplikace
        // Načtení FXML souboru pro uživatelské rozhraní
        FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("hello-view.fxml")));

        // Vytvoření scény s načteným uživatelským rozhraním a specifikací rozměrů
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);

        // Načtení CSS souboru pro stylizaci aplikace
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());

        // Nastavení názvu okna aplikace
        primaryStage.setTitle("Synthesiser");
        // Nastavení scény pro primární stage
        primaryStage.setScene(scene);
        // Zobrazení okna aplikace
        primaryStage.show();
    }

    public static void main(String[] args) { // Hlavní metoda pro spuštění aplikace
        launch(); // Volání metody launch pro zahájení aplikace
    }
}

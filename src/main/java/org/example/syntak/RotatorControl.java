package org.example.syntak;

import javafx.beans.property.DoubleProperty; // Import třídy DoubleProperty pro práci s dvojkovými vlastnostmi
import javafx.beans.property.SimpleDoubleProperty; // Import třídy SimpleDoubleProperty pro jednoduchou implementaci DoubleProperty
import javafx.geometry.Pos; // Import pro práci s geometrickým zarovnáním
import javafx.scene.image.Image; // Import pro práci s obrázky
import javafx.scene.image.ImageView; // Import pro zobrazení obrázků
import javafx.scene.layout.VBox; // Import pro vertikální kontejner
import javafx.scene.text.Text; // Import pro práci s textovými prvky

import java.util.Objects; // Import třídy Objects pro práci s objekty

public class RotatorControl extends VBox { // Třída RotatorControl, která dědí od VBox
    private final DoubleProperty knobRotation = new SimpleDoubleProperty(0); // Vlastnost pro uchování rotace knobu
    private double minValue = 0; // Minimální hodnota rotace
    private double maxValue = 270; // Maximální hodnota rotace
    private double initialMouseY; // Počáteční Y pozice myši pro rotaci
    private final ImageView dialImage; // Zobrazovač pro knob
    private final Text label; // Textový štítek pro popis knobu

    // Konstruktor s parametrem labelText
    public RotatorControl(String labelText) {
        // Načtení obrázku knobu
        Image knobImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/knob_image.png")));
        dialImage = new ImageView(knobImage); // Vytvoření ImageView pro knob
        dialImage.setFitWidth(60); // Nastavení šířky knobu
        dialImage.setFitHeight(60); // Nastavení výšky knobu

        label = new Text(labelText); // Vytvoření textového štítku
        label.getStyleClass().add("text"); // Přidání stylu k textu

        this.setSpacing(5); // Nastavení mezery mezi komponenty
        this.setAlignment(Pos.CENTER); // Zarovnání komponent uprostřed
        this.getStyleClass().add("knob-container"); // Přidání stylu k kontejneru

        this.getChildren().addAll(dialImage, label); // Přidání knobu a štítku do kontejneru

        // Události myši pro rotaci knobu
        dialImage.setOnMousePressed(event -> initialMouseY = event.getSceneY()); // Uložení počáteční pozice myši při stisknutí
        dialImage.setOnMouseDragged(event -> { // Akce při tažení knobu
            double deltaY = initialMouseY - event.getSceneY(); // Výpočet změny pozice myši
            double rotationChange = deltaY * 0.5; // Výpočet změny rotace
            setKnobRotation(getKnobRotation() + rotationChange); // Aktualizace rotace knobu
            initialMouseY = event.getSceneY(); // Aktualizace počáteční pozice myši
        });
    }

    public DoubleProperty knobRotationProperty() {
        return knobRotation; // Vrací vlastnost rotace knobu
    }

    public void setKnobRotation(double angle) {
        // Nastavení rotace knobu a omezení na minimální a maximální hodnoty
        if (angle < minValue) {
            knobRotation.set(minValue); // Pokud je úhel menší než minimální, nastaví se na minimální hodnotu
        } else {
            knobRotation.set(Math.min(angle, maxValue)); // Jinak se nastaví na maximální hodnotu
        }
        dialImage.setRotate(knobRotation.get()); // Nastavení rotace obrázku knobu
    }

    public double getKnobRotation() {
        return knobRotation.get(); // Vrací aktuální rotaci knobu
    }

    public void setMin(double min) {
        this.minValue = min; // Nastavení minimální hodnoty rotace
    }

    public void setMax(double max) {
        this.maxValue = max; // Nastavení maximální hodnoty rotace
    }
}

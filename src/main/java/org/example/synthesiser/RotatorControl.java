
package org.example.synthesiser;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class RotatorControl extends StackPane {
    // Vlastní property pro hodnotu rotace knobu
    private DoubleProperty knobRotation = new SimpleDoubleProperty(0);
    private double minValue = 0;
    private double maxValue = 270; // výchozí maximální hodnota

    // Volitelný prvek pro vizuální reprezentaci – zde jednoduchý kruh s textem
    private Circle dial;
    private Text label;

    public RotatorControl() {
        // Inicializace vizuální části
        dial = new Circle(30);  // poloměr 30 px
        dial.setStyle("-fx-fill: #555555; -fx-stroke: #CCCCCC; -fx-stroke-width: 2;");
        label = new Text("0°");
        label.setStyle("-fx-fill: #FFFFFF; -fx-font-size: 14px;");

        this.getChildren().addAll(dial, label);

        // Přidáme posluchač, který reaguje na změny vlastního knobRotation
        knobRotation.addListener((obs, oldVal, newVal) -> {
            // Aktualizace textu – zobrazí aktuální úhel
            label.setText(String.format("%.0f°", newVal.doubleValue()));
            // Volitelně: otočení vizuální reprezentace (pokud chceme, aby se dial vizuálně otáčel)
            this.setRotate(newVal.doubleValue());
        });

        // Příklad obsluhy události myši – při kliknutí se zvýší hodnota o 10°
        this.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            setKnobRotation(getKnobRotation() + 10);
        });
    }

    // Vlastní getter pro knobRotation property
    public DoubleProperty knobRotationProperty() {
        return knobRotation;
    }

    // Vlastní metoda pro nastavení hodnoty knobu – zajistí, že hodnota zůstane v intervalu [minValue, maxValue]
    public void setKnobRotation(double angle) {
        if (angle < minValue) {
            knobRotation.set(minValue);
        } else if (angle > maxValue) {
            knobRotation.set(maxValue);
        } else {
            knobRotation.set(angle);
        }
    }

    // Getter pro aktuální hodnotu knobRotation
    public double getKnobRotation() {
        return knobRotation.get();
    }

    // Metoda pro nastavení minimální hodnoty
    public void setMin(double min) {
        this.minValue = min;
    }

    // Metoda pro nastavení maximální hodnoty
    public void setMax(double max) {
        this.maxValue = max;
    }
}

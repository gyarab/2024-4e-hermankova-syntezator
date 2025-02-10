package org.example.synthesiser;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class RotatorControl extends VBox {
    private DoubleProperty knobRotation = new SimpleDoubleProperty(0);
    private double minValue = 0;
    private double maxValue = 270;
    private double initialMouseY;
    private ImageView dialImage;
    private Text label;

    public RotatorControl(String labelText) {
        // Načtení obrázku knobu
        Image knobImage = new Image(getClass().getResourceAsStream("/images/knob_image.png"));
        dialImage = new ImageView(knobImage);
        dialImage.setFitWidth(60);
        dialImage.setFitHeight(60);

        // Vytvoření popisku
        label = new Text(labelText);
        label.setStyle("-fx-fill: #FFFFFF; -fx-font-size: 14px;");

        // Uspořádání knobu a popisku ve VBoxu
        this.setSpacing(5);
        this.setAlignment(Pos.CENTER);
        this.getChildren().addAll(dialImage, label);

        // Obsluha událostí myši pro otáčení knobu tahem
        dialImage.setOnMousePressed(event -> initialMouseY = event.getSceneY());

        dialImage.setOnMouseDragged(event -> {
            double deltaY = initialMouseY - event.getSceneY();
            double rotationChange = deltaY * 0.5; // Nastavte citlivost podle potřeby
            setKnobRotation(getKnobRotation() + rotationChange);
            initialMouseY = event.getSceneY();
        });
    }

    public DoubleProperty knobRotationProperty() {
        return knobRotation;
    }

    public void setKnobRotation(double angle) {
        if (angle < minValue) {
            knobRotation.set(minValue);
        } else if (angle > maxValue) {
            knobRotation.set(maxValue);
        } else {
            knobRotation.set(angle);
        }
        dialImage.setRotate(knobRotation.get());
    }

    public double getKnobRotation() {
        return knobRotation.get();
    }

    public void setMin(double min) {
        this.minValue = min;
    }

    public void setMax(double max) {
        this.maxValue = max;
    }
}

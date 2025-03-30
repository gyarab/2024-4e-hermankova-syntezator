package org.example.syntak;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.Objects;

public class RotatorControl extends VBox {
    private final DoubleProperty knobRotation = new SimpleDoubleProperty(0);
    private double minValue = 0;
    private double maxValue = 270;
    private double initialMouseY;
    private final ImageView dialImage;
    private final Text label;

    // Constructor with labelText parameter
    public RotatorControl(String labelText) {
        Image knobImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/knob_image.png")));
        dialImage = new ImageView(knobImage);
        dialImage.setFitWidth(60);
        dialImage.setFitHeight(60);

        label = new Text(labelText);
        label.getStyleClass().add("text");

        this.setSpacing(5);
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("knob-container"); // Add style class

        this.getChildren().addAll(dialImage, label);

        // Mouse events for knob rotation
        dialImage.setOnMousePressed(event -> initialMouseY = event.getSceneY());
        dialImage.setOnMouseDragged(event -> {
            double deltaY = initialMouseY - event.getSceneY();
            double rotationChange = deltaY * 0.5;
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
        } else {
            knobRotation.set(Math.min(angle, maxValue));
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

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public String getLabelText() {
        return label.getText();
    }
}

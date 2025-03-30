package org.example.syntak;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;
import javafx.animation.AnimationTimer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SynthController {

    @FXML
    private Canvas oscilloscopeCanvas;

    @FXML
    private Pane knobContainer;

    @FXML
    private Button sineButton, squareButton, sawButton, startButton;

    private SynthEngine synthEngine;
    private static final Logger logger = Logger.getLogger(SynthController.class.getName());

    @FXML
    public void initialize() {
        synthEngine = new SynthEngine();
        setupKnobs();
        setupWaveButtons();
        setupStartButton();
        startOscilloscope();
    }

    private void setupKnobs() {
        String[] knobs = {"Volume", "Tune", "Width", "Color", "Depth", "Attack", "Decay", "Sustain", "Release"};
        for (String knob : knobs) {
            RotatorControl rotator = new RotatorControl(knob);
            rotator.setMin(0);
            rotator.setMax(270);

            double range = getParameterRange(knob);
            double initialRotation = (synthEngine.getParameter(knob.toLowerCase()) - getMinParameterValue(knob)) / range * 270;
            rotator.setKnobRotation(initialRotation);

            rotator.knobRotationProperty().addListener((_, _, newValue) -> {
                double value = newValue.doubleValue() / 270 * range + getMinParameterValue(knob);
                synthEngine.updateParameter(knob.toLowerCase(), value);
                logger.log(Level.INFO, "Knob updated: " + knob + " = " + value);
            });

            knobContainer.getChildren().add(rotator);
        }
    }

    private double getParameterRange(String knob) {
        return switch (knob) {
            case "Tune" -> 2000;
            case "Attack", "Decay", "Release" -> 2.0;
            default -> 1.0;
        };
    }

    private double getMinParameterValue(String knob) {
        if (knob.equals("Tune")) {
            return -1000;
        }
        return 0;
    }

    private void setupWaveButtons() {
        sineButton.setOnAction(_ -> synthEngine.setWaveType("sine"));
        squareButton.setOnAction(_ -> synthEngine.setWaveType("square"));
        sawButton.setOnAction(_ -> synthEngine.setWaveType("saw"));
    }

    private void setupStartButton() {
        startButton.setOnAction(_ -> {
            if (synthEngine.isPlaying()) {
                synthEngine.stop();
                startButton.setText("Start");
            } else {
                synthEngine.start();
                startButton.setText("Stop");
            }
        });
    }

    private void startOscilloscope() {
        GraphicsContext gc = oscilloscopeCanvas.getGraphicsContext2D();
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double[] waveData = synthEngine.getWaveform(512);
                gc.clearRect(0, 0, oscilloscopeCanvas.getWidth(), oscilloscopeCanvas.getHeight());
                gc.setStroke(Color.LIME);
                gc.setLineWidth(2);
                for (int i = 0; i < waveData.length - 1; i++) {
                    double x1 = i * oscilloscopeCanvas.getWidth() / waveData.length;
                    double y1 = oscilloscopeCanvas.getHeight() / 2 - waveData[i] * 50;
                    double x2 = (i + 1) * oscilloscopeCanvas.getWidth() / waveData.length;
                    double y2 = oscilloscopeCanvas.getHeight() / 2 - waveData[i + 1] * 50;
                    gc.strokeLine(x1, y1, x2, y2);
                }
            }
        };
        timer.start();
    }
}
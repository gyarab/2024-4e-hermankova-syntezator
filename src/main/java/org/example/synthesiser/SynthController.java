package org.example.synthesiser;

import com.oracle.javafx.scenebuilder.kit.util.control.paintpicker.rotator.RotatorControl;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class SynthController {

    @FXML
    private Canvas oscilloscopeCanvas;

    @FXML
    private Pane knobContainer;

    @FXML
    private Button sineButton, squareButton, sawButton, startButton;

    private SynthEngine synthEngine;

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
            rotator.setRotate(0); // Start at 0 degrees

            // Listener pro aktualizaci parametrů syntetizátoru
            rotator.rotationProperty().addListener((observable, oldValue, newValue) -> {
                double value = (newValue.doubleValue() / 270) * getParameterRange(knob);
                synthEngine.updateParameter(knob.toLowerCase(), value);
            });

            knobContainer.getChildren().add(rotator);
        }
    }

    private double getParameterRange(String knob) {
        switch (knob) {
            case "Volume":
                return 1.0; // 0 to 1
            case "Tune":
                return 2000; // -1000 to 1000 Hz
            case "Width":
                return 1.0; // 0 to 1
            case "Color":
                return 1.0; // 0 to 1
            case "Depth":
                return 1.0; // 0 to 1
            case "Attack":
                return 2.0; // 0 to 2 seconds
            case "Decay":
                return 2.0; // 0 to 2 seconds
            case "Sustain":
                return 1.0; // 0 to 1
            case "Release":
                return 2.0; // 0 to 2 seconds
            default:
                return 1.0;
        }
    }

    private void setupWaveButtons() {
        sineButton.setOnAction(e -> synthEngine.setWaveType("sine"));
        squareButton.setOnAction(e -> synthEngine.setWaveType("square"));
        sawButton.setOnAction(e -> synthEngine.setWaveType("saw"));
    }

    private void setupStartButton() {
        startButton.setOnAction(e -> {
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
        new Thread(() -> {
            while (true) {
                double[] waveData = synthEngine.getWaveform(); // Získání waveform podle aktuálního typu
                gc.clearRect(0, 0, oscilloscopeCanvas.getWidth(), oscilloscopeCanvas.getHeight());
                gc.setStroke(Color.LIME);
                gc.setLineWidth(2);
                for (int i = 0; i < waveData.length - 1; i++) {
                    double x1 = i * oscilloscopeCanvas.getWidth() / waveData.length;
                    double y1 = oscilloscopeCanvas.getHeight() / 2 - waveData[i] * oscilloscopeCanvas.getHeight() / 2;
                    double x2 = (i + 1) * oscilloscopeCanvas.getWidth() / waveData.length;
                    double y2 = oscilloscopeCanvas.getHeight() / 2 - waveData[i + 1] * oscilloscopeCanvas.getHeight() / 2;
                    gc.strokeLine(x1, y1, x2, y2);
                }
                try {
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException ignored) {
                }
            }
        }).start();
    }
}

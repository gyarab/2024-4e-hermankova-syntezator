package org.example.synthesiser;

import javafx.animation.AnimationTimer;
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
    private byte[] buffer;
    private static final int BUFFER_SIZE = 1024;

    @FXML
    public void initialize() {
        synthEngine = new SynthEngine();
        buffer = new byte[BUFFER_SIZE];
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

            double initialRotation;
            if (knob.equalsIgnoreCase("Tune")) {
                initialRotation = ((synthEngine.getParameter("tune") + 1000) / 2000.0) * 270;
            } else {
                initialRotation = (synthEngine.getParameter(knob.toLowerCase()) / getParameterRange(knob)) * 270;
            }
            rotator.setKnobRotation(initialRotation);

            rotator.knobRotationProperty().addListener((observable, oldValue, newValue) -> {
                double value = (newValue.doubleValue() / 270) * getParameterRange(knob);
                if (knob.equalsIgnoreCase("Tune")) {
                    value -= 1000;
                }
                synthEngine.updateParameter(knob.toLowerCase(), value + (knob.equalsIgnoreCase("Tune") ? 1000 : 0));
            });

            knobContainer.getChildren().add(rotator);
        }
    }

    private double getParameterRange(String knob) {
        switch (knob) {
            case "Volume": return 1.0;
            case "Tune": return 2000;
            case "Width": return 1.0;
            case "Color": return 1.0;
            case "Depth": return 1.0;
            case "Attack": return 2.0;
            case "Decay": return 2.0;
            case "Sustain": return 1.0;
            case "Release": return 2.0;
            default: return 1.0;
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
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double[] waveData = synthEngine.getWaveform(buffer.length / 2);
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
            }
        };
        timer.start();
    }
}

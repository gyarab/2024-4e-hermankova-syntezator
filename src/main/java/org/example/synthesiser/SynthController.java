
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

    // Doporučuji použít např. FlowPane pro lepší rozmístění, ale zde ponecháme Pane
    @FXML
    private Pane knobContainer;

    @FXML
    private Button sineButton, squareButton, sawButton, startButton;

    private SynthEngine synthEngine;
    // Buffer sloužící pro vizualizaci – počet vzorků odpovídá polovině velikosti audio bufferu
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
            // Vytvoříme instanci našeho vlastního RotatorControl
            RotatorControl rotator = new RotatorControl();
            rotator.setMin(0);
            rotator.setMax(270);  // Maximální úhel 270°
            rotator.setKnobRotation(0); // Výchozí hodnota

            // Listener pro aktualizaci parametrů syntetizátoru
            // Používáme knobRotationProperty() naší třídy RotatorControl
            rotator.knobRotationProperty().addListener((observable, oldValue, newValue) -> {
                double value = (newValue.doubleValue() / 270) * getParameterRange(knob);
                synthEngine.updateParameter(knob.toLowerCase(), value);
            });

            knobContainer.getChildren().add(rotator);
        }
    }

    private double getParameterRange(String knob) {
        switch (knob) {
            case "Volume":
                return 1.0; // 0 až 1
            case "Tune":
                return 2000; // -1000 až 1000 Hz (pozn.: případný offset lze doladit)
            case "Width":
                return 1.0; // 0 až 1
            case "Color":
                return 1.0; // 0 až 1
            case "Depth":
                return 1.0; // 0 až 1
            case "Attack":
                return 2.0; // 0 až 2 sekundy
            case "Decay":
                return 2.0; // 0 až 2 sekundy
            case "Sustain":
                return 1.0; // 0 až 1
            case "Release":
                return 2.0; // 0 až 2 sekundy
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

    // Osciloskop aktualizujeme pomocí AnimationTimer (běží v JavaFX vlákně)
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

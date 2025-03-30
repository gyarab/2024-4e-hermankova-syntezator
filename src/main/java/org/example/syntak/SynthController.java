package org.example.syntak;

import javafx.fxml.FXML; // Import pro anotaci FXML
import javafx.scene.canvas.Canvas; // Import pro práci s plátnem (canvas)
import javafx.scene.control.Button; // Import pro tlačítka
import javafx.scene.layout.Pane; // Import pro práci s panelem
import javafx.scene.paint.Color; // Import pro práci s barvami
import javafx.scene.canvas.GraphicsContext; // Import pro grafický kontext plátna
import javafx.animation.AnimationTimer; // Import pro animaci
import java.util.logging.Level; // Import pro úroveň logování
import java.util.logging.Logger; // Import pro logování

public class SynthController { // Třída SynthController, která řídí interakce uživatelského rozhraní

    @FXML
    private Canvas oscilloscopeCanvas; // Plátno pro zobrazení osciloskopu

    @FXML
    private Pane knobContainer; // Kontejner pro ovládací knoby

    @FXML
    private Button sineButton, squareButton, sawButton, startButton; // Tlačítka pro výběr vlny a start/zastavení

    private SynthEngine synthEngine; // Instance zvukového enginu
    private static final Logger logger = Logger.getLogger(SynthController.class.getName()); // Logger pro záznam událostí

    @FXML
    public void initialize() { // Metoda, která se volá při inicializaci kontroléru
        synthEngine = new SynthEngine(); // Inicializace zvukového enginu
        // Pro testování lze zapnout pevnou obálku:
        // synthEngine.setTestEnvelope(true);
        setupKnobs(); // Nastavení ovládacích knobů
        setupWaveButtons(); // Nastavení tlačítek pro výběr vln
        setupStartButton(); // Nastavení tlačítka pro start/zastavení
        startOscilloscope(); // Spuštění osciloskopu
    }

    private void setupKnobs() { // Metoda pro nastavení ovládacích knobů
        String[] knobs = {"Volume", "Tune", "Width", "Color", "Depth", "Attack", "Decay", "Sustain", "Release"}; // Seznam knobů
        for (String knob : knobs) { // Pro každý knob v seznamu
            RotatorControl rotator = new RotatorControl(knob); // Vytvoření nového otočného knobu
            rotator.setMin(0); // Nastavení minimální hodnoty rotace
            rotator.setMax(270); // Nastavení maximální hodnoty rotace

            double range = getParameterRange(knob); // Získání rozsahu pro aktuální knob
            double initialRotation = (synthEngine.getParameter(knob.toLowerCase()) - getMinParameterValue(knob)) / range * 270; // Výpočet počáteční rotace
            rotator.setKnobRotation(initialRotation); // Nastavení počáteční rotace knobu

            // Přidání listeneru pro aktualizaci parametru zvuku při změně rotace knobu
            rotator.knobRotationProperty().addListener((_, __, newValue) -> {
                double value = newValue.doubleValue() / 270 * range + getMinParameterValue(knob); // Přepočet hodnoty na skutečný parametr
                synthEngine.updateParameter(knob.toLowerCase(), value); // Aktualizace parametru ve zvukovém enginu
                logger.log(Level.INFO, "Knob updated: " + knob + " = " + value); // Logování změny hodnoty knobu
            });

            knobContainer.getChildren().add(rotator); // Přidání knobu do kontejneru
        }
    }

    private double getParameterRange(String knob) { // Metoda pro získání rozsahu pro každý knob
        return switch (knob) {
            case "Tune" -> 2000; // Rozsah pro ladění
            case "Attack", "Decay", "Release" -> 2.0; // Rozsah pro obálku
            default -> 1.0; // Rozsah pro ostatní knoby (0 až 1)
        };
    }

    private double getMinParameterValue(String knob) { // Metoda pro získání minimální hodnoty parametru
        if (knob.equals("Tune")) {
            return -1000; // Minimální hodnota pro ladění
        }
        return 0; // Minimální hodnota pro ostatní knoby
    }

    private void setupWaveButtons() { // Metoda pro nastavení tlačítek pro výběr vln
        sineButton.setOnAction(_ -> synthEngine.setWaveType("sine")); // Nastavení vlny na sínusovou
        squareButton.setOnAction(_ -> synthEngine.setWaveType("square")); // Nastavení vlny na obdélníkovou
        sawButton.setOnAction(_ -> synthEngine.setWaveType("saw")); // Nastavení vlny na pilovitou
    }

    private void setupStartButton() { // Metoda pro nastavení tlačítka pro start/zastavení
        startButton.setOnAction(_ -> { // Akce při kliknutí na tlačítko
            if (synthEngine.isPlaying()) { // Pokud je zvuk aktivní
                synthEngine.stop(); // Zastavení zvuku
                startButton.setText("Start"); // Změna textu na "Start"
            } else { // Pokud zvuk nehraje
                synthEngine.start(); // Spuštění zvuku
                startButton.setText("Stop"); // Změna textu na "Stop"
            }
        });
    }

    private void startOscilloscope() { // Metoda pro spuštění osciloskopu
        GraphicsContext gc = oscilloscopeCanvas.getGraphicsContext2D(); // Získání grafického kontextu plátna
        AnimationTimer timer = new AnimationTimer() { // Vytvoření animace pro aktualizaci osciloskopu
            @Override
            public void handle(long now) { // Metoda, která se volá v každém snímku animace
                double[] waveData = synthEngine.getWaveform(512); // Získání dat vlny ze zvukového enginu
                gc.clearRect(0, 0, oscilloscopeCanvas.getWidth(), oscilloscopeCanvas.getHeight()); // Vymazání plátna

                // Dynamické škálování: najdeme maximální absolutní hodnotu v bufferu
                double maxAmp = 0; // Inicializace proměnné pro maximální amplitudu
                for (double v : waveData) { // Pro každou hodnotu ve waveData
                    maxAmp = Math.max(maxAmp, Math.abs(v)); // Aktualizace maximální amplitudy
                }
                // Pokud není amplituda nulová, spočítáme škálovací faktor
                double scaleFactor = (maxAmp > 0) ? (oscilloscopeCanvas.getHeight() * 0.45) / maxAmp : 50; // Výpočet škálovacího faktoru

                gc.setStroke(Color.LIME); // Nastavení barvy čáry
                gc.setLineWidth(2); // Nastavení šířky čáry
                for (int i = 0; i < waveData.length - 1; i++) { // Pro každou hodnotu ve waveData
                    double x1 = i * oscilloscopeCanvas.getWidth() / waveData.length; // Výpočet x pozice pro první bod
                    double y1 = oscilloscopeCanvas.getHeight() / 2 - waveData[i] * scaleFactor; // Výpočet y pozice pro první bod
                    double x2 = (i + 1) * oscilloscopeCanvas.getWidth() / waveData.length; // Výpočet x pozice pro druhý bod
                    double y2 = oscilloscopeCanvas.getHeight() / 2 - waveData[i + 1] * scaleFactor; // Výpočet y pozice pro druhý bod
                    gc.strokeLine(x1, y1, x2, y2); // Kreslení čáry mezi dvěma body
                }
            }
        };
        timer.start(); // Spuštění animace
    }
}

package org.example.syntak;

import javax.sound.sampled.*; // Import pro práci se zvukovými operacemi
import java.util.logging.Level; // Import pro úroveň logování
import java.util.logging.Logger; // Import pro logování

public class SynthEngine { // Třída SynthEngine zajišťující generaci zvuku

    private String waveType = "sine"; // Typ vlny (výchozí je sínusová)
    private boolean playing = false; // Stav, zda se zvuk přehrává
    public FloatControl volumeControl; // Ovladač pro hlasitost
    private SourceDataLine line; // Zvuková linka pro přehrávání zvuku

    // Parametry syntézy
    private double volume = 1.0;   // Hlasitost v rozsahu 0.0 až 1.0
    private double tune = 440.0;    // Frekvence v Hz; interní rozsah -1000 až 1000
    private double width = 1.0;     // 0.0 až 1.0 – ovlivňuje duty cycle u čtvercové vlny
    private double color = 1.0;     // 0.0 až 1.0 – ovlivňuje mix sinusové a kosinusové složky (změna timbře)
    private double depth = 1.0;     // 0.0 až 1.0 – u pilovité vlny přidává harmonickou složku
    private double attack = 0.1;    // Čas v sekundách (0 až 2) pro fázi nástupu
    private double decay = 0.1;     // Čas v sekundách (0 až 2) pro fázi poklesu
    private double sustain = 1.0;   // Udržovací úroveň v rozsahu 0.0 až 1.0
    private double release = 0.1;   // Čas v sekundách (0 až 2) pro fázi uvolnění

    // Fáze – kontinuálně narůstá mezi buffery (nikoli resetována)
    private double phase = 0.0; // Aktuální fáze signálu
    private final double sampleRate = 44100.0; // Vzorkovací frekvence

    // Pokud je nastaveno na true, tak se obálka (envelope) ignoruje a používá se pevná hodnota 1.0 – vhodné při testování
    private boolean testEnvelope = false;

    private static final Logger logger = Logger.getLogger(SynthEngine.class.getName()); // Logger pro záznam událostí

    public SynthEngine() { // Konstruktor třídy
        initAudioLine(); // Inicializace zvukové linky
    }

    private void initAudioLine() { // Metoda pro inicializaci zvukové linky
        try {
            AudioFormat format = new AudioFormat((float) sampleRate, 16, 1, true, true); // Nastavení formátu zvuku
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format); // Informace o datové lince
            line = (SourceDataLine) AudioSystem.getLine(info); // Získání zvukové linky
            line.open(format); // Otevření linky
            line.start(); // Spuštění linky
            volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN); // Získání ovladače hlasitosti
        } catch (LineUnavailableException e) {
            logger.log(Level.SEVERE, "Audio line unavailable", e); // Záznam chyby, pokud není linka k dispozici
        }
    }

    public void setWaveType(String waveType) { // Metoda pro nastavení typu vlny
        this.waveType = waveType.toLowerCase(); // Převedení typu na malé písmeno
    }

    public void start() { // Metoda pro spuštění zvuku
        if (!playing) { // Pokud zvuk nehraje
            playing = true; // Nastavení stavu na hrající
            initAudioLine(); // Znovu inicializace linky při spuštění
            new Thread(this::generateSound, "SynthSoundThread").start(); // Spuštění generování zvuku v novém vlákně
        }
    }

    public void stop() { // Metoda pro zastavení zvuku
        playing = false; // Nastavení stavu na nehrájící
    }

    protected boolean isPlaying() { // Metoda pro kontrolu, zda zvuk hraje
        return playing; // Vrací aktuální stav přehrávání
    }

    public double getParameter(String parameter) { // Metoda pro získání hodnoty parametru
        return switch (parameter) { // Přepínač pro vrácení hodnoty na základě názvu parametru
            case "volume" -> volume;
            case "tune" -> tune;
            case "width" -> width;
            case "color" -> color;
            case "depth" -> depth;
            case "attack" -> attack;
            case "decay" -> decay;
            case "sustain" -> sustain;
            case "release" -> release;
            default -> 0; // Výchozí hodnota, pokud není parametr nalezen
        };
    }

    public void updateParameter(String parameter, double value) { // Metoda pro aktualizaci hodnoty parametru
        switch (parameter) { // Přepínač pro aktualizaci hodnoty na základě názvu parametru
            case "volume": volume = Math.max(0, Math.min(value, 1)); break; // Omezit hodnotu hlasitosti na 0 až 1
            case "tune":
                tune = Math.max(-1000, Math.min(value, 1000)); // Omezit ladění na -1000 až 1000
                // Upozornění: Pokud je absolutní hodnota tune blízko Nyquistovy frekvence (sampleRate/2), může dojít k aliasingu.
                if (Math.abs(tune) > sampleRate / 2 * 0.9) {
                    logger.log(Level.WARNING, "Tune je nastaveno příliš vysoko a může dojít k aliasingu!"); // Záznam varování
                }
                break;
            case "width": width = Math.max(0, Math.min(value, 1)); break; // Omezit šířku na 0 až 1
            case "color": color = Math.max(0, Math.min(value, 1)); break; // Omezit barvu na 0 až 1
            case "depth": depth = Math.max(0, Math.min(value, 1)); break; // Omezit hloubku na 0 až 1
            case "attack": attack = Math.max(0, Math.min(value, 2)); break; // Omezit attack na 0 až 2 sekundy
            case "decay": decay = Math.max(0, Math.min(value, 2)); break; // Omezit decay na 0 až 2 sekundy
            case "sustain": sustain = Math.max(0, Math.min(value, 1)); break; // Omezit sustain na 0 až 1
            case "release": release = Math.max(0, Math.min(value, 2)); break; // Omezit release na 0 až 2 sekundy
        }
    }

    private void generateSound() { // Metoda pro generování zvuku
        byte[] buffer = new byte[4096]; // 4096 bajtů = 2048 vzorků
        try {
            while (playing) { // Dokud zvuk hraje
                double[] waveData = getWaveform(buffer.length / 2); // Získání dat vlny
                for (int i = 0; i < buffer.length; i += 2) { // Pro každý vzorek
                    int sample = (int) (waveData[i / 2] * 32767); // Převedení hodnoty vlny na vzorek
                    buffer[i] = (byte) (sample & 0xFF); // Uložení spodního bajtu vzorku
                    buffer[i + 1] = (byte) ((sample >> 8) & 0xFF); // Uložení horního bajtu vzorku
                }
                line.write(buffer, 0, buffer.length); // Zápis bufferu do zvukové linky
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while generating sound", e); // Záznam chyby při generování zvuku
        } finally {
            if (line != null) { // Kontrola, zda je zvuková linka inicializována
                line.stop(); // Zastavení linky
                line.close(); // Uzavření linky
                line = null; // Uvolnění reference na linku
            }
        }
    }

    public double[] getWaveform(int sampleCount) { // Metoda pro získání vlnové formy
        double[] wave = new double[sampleCount]; // Pole pro vzorky vlny
        double increment = (2 * Math.PI * tune) / sampleRate; // Výpočet inkrementu pro fázi
        double envTotalTime = attack + decay + release; // Celkový čas obálky

        for (int i = 0; i < sampleCount; i++) { // Pro každý vzorek
            // Výpočet obálky – v testovacím režimu se používá pevná hodnota 1.0
            double env;
            if (testEnvelope) {
                env = 1.0; // Pevná hodnota pro testovací režim
            } else {
                double tEnv = (i / (double) sampleCount) * envTotalTime; // Výpočet času obálky
                if (tEnv < attack) {
                    env = tEnv / attack; // Nástup
                } else if (tEnv < attack + decay) {
                    env = 1 - ((tEnv - attack) / decay) * (1 - sustain); // Pokles
                } else if (tEnv < envTotalTime) {
                    env = sustain * (1 - ((tEnv - (attack + decay)) / release)); // Udržení
                } else {
                    env = sustain; // Konec obálky
                }
            }

            double value; // Proměnná pro hodnotu vlny
            switch (waveType) { // Přepínač pro různé typy vln
                case "sine" -> {
                    // Míchání sinus a kosinus na základě parametru color; normalizace, aby amplituda zůstala 1.
                    double s = Math.sin(phase); // Sínusová složka
                    double c = Math.cos(phase); // Kosinusová složka
                    double mix = (1 - color) * s + color * c; // Míchání
                    double norm = Math.sqrt((1 - color) * (1 - color) + color * color); // Normalizace
                    value = mix / norm; // Výsledná hodnota
                }
                case "square" -> {
                    // Proměnná duty cycle určená parametrem width.
                    double period = 2 * Math.PI; // Délka periody
                    double currentPhase = phase % period; // Aktuální fáze
                    value = (currentPhase < period * width) ? 1 : -1; // Čtvercová vlna
                }
                case "saw" -> {
                    // Základní saw wave + přidána druhá harmonická složka modifikovaná parametrem depth.
                    double period = 2 * Math.PI; // Délka periody
                    double currentPhase = phase % period; // Aktuální fáze
                    value = 2 * (currentPhase / period) - 1; // Generování pilovité vlny
                    value += depth * 0.5 * Math.sin(2 * phase); // Přidání druhé harmonické složky
                    // Omezíme výslednou amplitudu na rozsah -1 až 1
                    value = Math.max(-1, Math.min(value, 1)); // Omezit hodnotu
                }
                default -> value = 0.0; // Výchozí hodnota
            }
            wave[i] = volume * value * env; // Uložení vzorku do pole
            phase += increment; // Aktualizace fáze
            // Odstraněno "resetování" fáze – phase kontinuálně narůstá napříč buffery
        }
        return wave; // Vrací generovanou vlnovou formu
    }

    // Možnost zapnout/vypnout testovací režim obálky (např. při ladění)
    public void setTestEnvelope(boolean testEnvelope) {
        this.testEnvelope = testEnvelope; // Nastavení testovacího režimu
    }
}

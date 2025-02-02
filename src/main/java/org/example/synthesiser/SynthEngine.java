package org.example.synthesiser;

import javax.sound.sampled.*;
import java.util.Arrays;

public class SynthEngine {

    private String waveType = "sine";
    private boolean playing = false;
    private FloatControl volumeControl;

    // Parametry syntézy
    private double volume = 1.0;   // 0.0 to 1.0
    private double tune = 440.0;    // Hz
    private double width = 1.0;      // 0.0 to 1.0
    private double color = 1.0;      // 0.0 to 1.0
    private double depth = 1.0;      // 0.0 to 1.0
    private double attack = 0.1;     // seconds
    private double decay = 0.1;      // seconds
    private double sustain = 1.0;    // 0.0 to 1.0
    private double release = 0.1;    // seconds

    public SynthEngine() {
        // Nastavení audio systému
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void setWaveType(String waveType) {
        this.waveType = waveType;
    }

    public void start() {
        playing = true;
        // Spustit syntézu zvuku
        new Thread(this::generateSound).start();
    }

    public void stop() {
        playing = false;
        // Zastavit syntézu zvuku
    }

    public boolean isPlaying() {
        return playing;
    }

    public void updateParameter(String parameter, double value) {
        // Aktualizovat specifický parametr
        switch (parameter) {
            case "volume":
                volume = value; // Uložení hodnoty hlasitosti
                float volumeDb = (float) (20 * Math.log10(value));
                volumeControl.setValue(volumeDb);
                break;
            case "tune":
                tune = value; // Uložení hodnoty ladění
                break;
            case "width":
                width = value; // Uložení šířky
                break;
            case "color":
                color = value; // Uložení barvy
                break;
            case "depth":
                depth = value; // Uložení hloubky
                break;
            case "attack":
                attack = value; // Uložení attack
                break;
            case "decay":
                decay = value; // Uložení decay
                break;
            case "sustain":
                sustain = value; // Uložení sustain
                break;
            case "release":
                release = value; // Uložení release
                break;
            default:
                break;
        }
    }

    private void generateSound() {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
            byte[] buffer = new byte[4096];

            while (playing) {
                // Generovat zvuk
                double[] waveData = getWaveform();
                for (int i = 0; i < buffer.length; i += 2) {
                    int sample = (int) (waveData[i / 2] * 32767);
                    buffer[i] = (byte) (sample & 0xFF);
                    buffer[i + 1] = (byte) ((sample >> 8) & 0xFF);
                }
                line.write(buffer, 0, buffer.length);
            }
            line.stop();
            line.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public double[] getWaveform() {
        // Simulovat waveform pro oscilloscope
        double[] wave = new double[100];
        double increment = (2 * Math.PI * tune) / 44100; // Vytvoření frekvence
        double phase = 0.0;

        for (int i = 0; i < wave.length; i++) {
            double t = i / (double) wave.length; // Časový krok

            switch (waveType) {
                case "sine":
                    wave[i] = volume * Math.sin(phase);
                    break;
                case "square":
                    wave[i] = volume * (Math.sin(phase) >= 0 ? 1 : -1);
                    break;
                case "saw":
                    wave[i] = volume * (2.0 * (t - Math.floor(t + 0.5)));
                    break;
            }

            // Přidání ADSR (Attack, Decay, Sustain, Release)
            if (i < attack * wave.length) {
                wave[i] *= (i / (attack * wave.length)); // Attack
            } else if (i < (attack + decay) * wave.length) {
                wave[i] *= (1 - (i - (attack * wave.length)) / (decay * wave.length)); // Decay
            } else if (i < (attack + decay + sustain) * wave.length) {
                wave[i] *= sustain; // Sustain
            } else if (i < wave.length) {
                wave[i] *= (1 - (i - ((attack + decay + sustain) * wave.length)) / ((release) * wave.length)); // Release
            }

            phase += increment; // Posun fáze pro další vzorek
            if (phase >= 2 * Math.PI) {
                phase -= 2 * Math.PI; // Udržení fáze v rozsahu
            }
        }
        return wave;
    }
}

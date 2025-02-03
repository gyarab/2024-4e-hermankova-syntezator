package org.example.synthesiser;

import javax.sound.sampled.*;

public class SynthEngine {

    private String waveType = "sine";
    private boolean playing = false;
    private FloatControl volumeControl;

    // Parametry syntézy
    private double volume = 1.0;   // 0.0 až 1.0
    private double tune = 440.0;    // Hz
    private double width = 1.0;
    private double color = 1.0;
    private double depth = 1.0;
    private double attack = 0.1;    // sekundy
    private double decay = 0.1;     // sekundy
    private double sustain = 1.0;   // 0.0 až 1.0
    private double release = 0.1;   // sekundy

    // Zachování kontinuity fáze mezi buffery
    private double phase = 0.0;
    private final double sampleRate = 44100.0;

    public SynthEngine() {
        // Inicializace audio systému, získání ovládání hlasitosti
        try {
            AudioFormat format = new AudioFormat((float) sampleRate, 16, 1, true, true);
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
        new Thread(this::generateSound, "SynthSoundThread").start();
    }

    public void stop() {
        playing = false;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void updateParameter(String parameter, double value) {
        switch (parameter) {
            case "volume":
                volume = value;
                if (value <= 0.0001) {
                    volumeControl.setValue(volumeControl.getMinimum());
                } else {
                    float volumeDb = (float) (20 * Math.log10(value));
                    volumeControl.setValue(volumeDb);
                }
                break;
            case "tune":
                tune = value;
                break;
            case "width":
                width = value;
                break;
            case "color":
                color = value;
                break;
            case "depth":
                depth = value;
                break;
            case "attack":
                attack = value;
                break;
            case "decay":
                decay = value;
                break;
            case "sustain":
                sustain = value;
                break;
            case "release":
                release = value;
                break;
            default:
                break;
        }
    }

    private void generateSound() {
        try {
            AudioFormat format = new AudioFormat((float) sampleRate, 16, 1, true, true);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
            byte[] buffer = new byte[4096]; // 4096 bajtů = 2048 vzorků

            while (playing) {
                double[] waveData = getWaveform(buffer.length / 2);
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

    /**
     * Vrací pole vzorků v rozsahu -1 až 1.
     * Simulace ADSR: pro každý buffer se počítá envelope na základě attack, decay a release.
     * Pokud je buffer delší, zbytek je nastaven na sustain.
     */
    public double[] getWaveform(int sampleCount) {
        double[] wave = new double[sampleCount];
        double increment = (2 * Math.PI * tune) / sampleRate;
        double envTotalTime = attack + decay + release;
        for (int i = 0; i < sampleCount; i++) {
            double tEnv = (i / (double) sampleCount) * envTotalTime;
            double env;
            if (tEnv < attack) {
                env = tEnv / attack;
            } else if (tEnv < attack + decay) {
                env = 1 - ((tEnv - attack) / decay) * (1 - sustain);
            } else if (tEnv < envTotalTime) {
                env = sustain * (1 - ((tEnv - (attack + decay)) / release));
            } else {
                env = sustain;
            }

            double value = 0.0;
            switch (waveType) {
                case "sine":
                    value = Math.sin(phase);
                    break;
                case "square":
                    value = Math.sin(phase) >= 0 ? 1 : -1;
                    break;
                case "saw":
                    value = 2.0 * (phase / (2 * Math.PI)) - 1;
                    break;
            }
            wave[i] = volume * value * env;
            phase += increment;
            if (phase >= 2 * Math.PI) {
                phase -= 2 * Math.PI;
            }
        }
        return wave;
    }
}
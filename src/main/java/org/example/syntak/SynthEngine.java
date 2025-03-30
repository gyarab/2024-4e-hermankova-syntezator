package org.example.syntak;

import javax.sound.sampled.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SynthEngine {

    private String waveType = "sine";
    private boolean playing = false;
    public FloatControl volumeControl;
    private SourceDataLine line;

    // Synthesis parameters
    private double volume = 1.0;   // 0.0 to 1.0
    private double tune = 440.0;    // Hz; internally we'll have a range of -1000 to 1000
    private double width = 1.0;
    private double color = 1.0;
    private double depth = 1.0;
    private double attack = 0.1;    // seconds (0 to 2)
    private double decay = 0.1;     // seconds (0 to 2)
    private double sustain = 1.0;   // 0.0 to 1.0
    private double release = 0.1;   // seconds (0 to 2)

    // Phase continuity between buffers
    private double phase = 0.0;
    private final double sampleRate = 44100.0;

    private static final Logger logger = Logger.getLogger(SynthEngine.class.getName());

    public SynthEngine() {
        initAudioLine();
    }

    private void initAudioLine() {
        try {
            AudioFormat format = new AudioFormat((float) sampleRate, 16, 1, true, true);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
        } catch (LineUnavailableException e) {
            logger.log(Level.SEVERE, "Audio line unavailable", e);
        }
    }

    public void setWaveType(String waveType) {
        this.waveType = waveType.toLowerCase();
    }

    public void start() {
        if (!playing) {
            playing = true;
            initAudioLine(); // Reinitialize line on start
            new Thread(this::generateSound, "SynthSoundThread").start();
        }
    }

    public void stop() {
        playing = false;
    }

    protected boolean isPlaying() {
        return playing;
    }

    public double getParameter(String parameter) {
        return switch (parameter) {
            case "volume" -> volume;
            case "tune" -> tune;
            case "width" -> width;
            case "color" -> color;
            case "depth" -> depth;
            case "attack" -> attack;
            case "decay" -> decay;
            case "sustain" -> sustain;
            case "release" -> release;
            default -> 0;
        };
    }

    public void updateParameter(String parameter, double value) {
        switch (parameter) {
            case "volume": volume = Math.max(0, Math.min(value, 1)); break;
            case "tune": tune = Math.max(-1000, Math.min(value, 1000)); break;
            case "width": width = Math.max(0, Math.min(value, 1)); break;
            case "color": color = Math.max(0, Math.min(value, 1)); break;
            case "depth": depth = Math.max(0, Math.min(value, 1)); break;
            case "attack": attack = Math.max(0, Math.min(value, 2)); break;
            case "decay": decay = Math.max(0, Math.min(value, 2)); break;
            case "sustain": sustain = Math.max(0, Math.min(value, 1)); break;
            case "release": release = Math.max(0, Math.min(value, 2)); break;
        }
    }


    private void generateSound() {
        byte[] buffer = new byte[4096]; // 4096 bytes = 2048 samples
        try {
            while (playing) {
                double[] waveData = getWaveform(buffer.length / 2);
                for (int i = 0; i < buffer.length; i += 2) {
                    int sample = (int) (waveData[i / 2] * 32767);
                    buffer[i] = (byte) (sample & 0xFF);
                    buffer[i + 1] = (byte) ((sample >> 8) & 0xFF);
                }
                line.write(buffer, 0, buffer.length);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while generating sound", e);
        } finally {
            if (line != null) {
                line.stop();
                line.close();
                line = null;
            }
        }
    }

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
            double value = switch (waveType) {
                case "sine" -> Math.sin(phase);
                case "square" -> Math.sin(phase) >= 0 ? 1 : -1;
                case "saw" -> 2.0 * (phase / (2 * Math.PI)) - 1;
                default -> 0.0;
            };
            wave[i] = volume * value * env;
            phase += increment;
            if (phase >= 2 * Math.PI) {
                phase -= 2 * Math.PI;
            }
        }
        return wave;
    }
}
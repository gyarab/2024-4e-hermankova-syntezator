package org.example.synthesiser;

import javax.sound.sampled.*;

public class SynthEngine {

    private String waveType = "sine";
    private boolean playing = false;
    private FloatControl volumeControl;
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
            e.printStackTrace();
            // Handle the exception, possibly by notifying the user or logging
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

    public boolean isPlaying() {
        return playing;
    }

    public double getParameter(String parameter) {
        switch (parameter) {
            case "volume": return volume;
            case "tune": return tune;
            case "width": return width;
            case "color": return color;
            case "depth": return depth;
            case "attack": return attack;
            case "decay": return decay;
            case "sustain": return sustain;
            case "release": return release;
            default: return 0;
        }
    }

    public void updateParameter(String parameter, double value) {
        switch (parameter) {
            case "volume":
                volume = value;
                if (volumeControl != null && value > 0.0001) {
                    float volumeDb = (float) (20 * Math.log10(value));
                    volumeControl.setValue(volumeDb);
                }
                break;
            case "tune":
                tune = value - 1000; // Adjust to the range -1000 to 1000 Hz
                break;
            case "width":  width = value; break;
            case "color":  color = value; break;
            case "depth":  depth = value; break;
            case "attack": attack = value; break;
            case "decay":  decay = value; break;
            case "sustain": sustain = value; break;
            case "release": release = value; break;
            default: break;
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
            e.printStackTrace(); // Log any exceptions
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

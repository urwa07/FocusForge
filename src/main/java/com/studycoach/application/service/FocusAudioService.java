package com.studycoach.application.service;

import javafx.scene.media.AudioClip;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat.Encoding;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class FocusAudioService {
    private static final int SAMPLE_RATE = 44100;
    private static final int DURATION_SECONDS = 10;

    private AudioClip ambientClip;
    private AudioClip rainClip;
    private boolean available;
    private boolean rainMode;

    public FocusAudioService() {
        try {
            Path audioDir = Path.of(System.getProperty("java.io.tmpdir"), "study-coach-audio");
            Files.createDirectories(audioDir);
            this.ambientClip = createClip(audioDir.resolve("ambient.wav"), false);
            this.rainClip = createClip(audioDir.resolve("rain.wav"), true);
            this.available = true;
        } catch (Throwable exception) {
            this.available = false;
        }
    }

    public void play(boolean rainMode) {
        if (!available) {
            return;
        }
        this.rainMode = rainMode;
        stop();
        AudioClip clip = rainMode ? rainClip : ambientClip;
        clip.setCycleCount(AudioClip.INDEFINITE);
        clip.setVolume(0.32);
        clip.play();
    }

    public void stop() {
        if (!available) {
            return;
        }
        ambientClip.stop();
        rainClip.stop();
    }

    public void setRainMode(boolean rainMode) {
        if (!available) {
            this.rainMode = rainMode;
            return;
        }
        this.rainMode = rainMode;
        if (isPlaying()) {
            play(rainMode);
        }
    }

    public boolean isRainMode() {
        return rainMode;
    }

    public boolean isPlaying() {
        if (!available) {
            return false;
        }
        return ambientClip.isPlaying() || rainClip.isPlaying();
    }

    private AudioClip createClip(Path file, boolean rain) throws IOException {
        if (Files.notExists(file)) {
            byte[] audioBytes = generateAudioBytes(rain);
            AudioFormat format = new AudioFormat(Encoding.PCM_SIGNED, SAMPLE_RATE, 16, 1, 2, SAMPLE_RATE, false);
            try (AudioInputStream inputStream = new AudioInputStream(new ByteArrayInputStream(audioBytes), format, audioBytes.length / 2L)) {
                AudioSystem.write(inputStream, javax.sound.sampled.AudioFileFormat.Type.WAVE, file.toFile());
            }
        }
        return new AudioClip(file.toUri().toString());
    }

    private byte[] generateAudioBytes(boolean rain) {
        int totalSamples = SAMPLE_RATE * DURATION_SECONDS;
        byte[] data = new byte[totalSamples * 2];
        Random random = new Random(rain ? 512L : 128L);
        double smoothing = 0.0;

        for (int i = 0; i < totalSamples; i++) {
            double t = i / (double) SAMPLE_RATE;
            double base = rain
                    ? (Math.sin(2 * Math.PI * 58 * t) * 0.03 + Math.sin(2 * Math.PI * 120 * t) * 0.02)
                    : (Math.sin(2 * Math.PI * 110 * t) * 0.03 + Math.sin(2 * Math.PI * 220 * t) * 0.02);
            double noise = random.nextGaussian() * (rain ? 0.09 : 0.04);
            smoothing = smoothing * 0.985 + noise;
            double drops = rain && random.nextDouble() < 0.0007 ? 0.55 : 0.0;
            double sampleValue = clamp(base + smoothing * (rain ? 0.7 : 0.35) + drops, -1.0, 1.0);
            short sample = (short) (sampleValue * Short.MAX_VALUE);
            data[i * 2] = (byte) (sample & 0xff);
            data[i * 2 + 1] = (byte) ((sample >> 8) & 0xff);
        }
        return data;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}

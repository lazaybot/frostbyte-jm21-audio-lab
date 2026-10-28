package com.frostbyte.theme;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;

final class ToneEngine {
    enum Channel { LEFT, RIGHT, BOTH }

    private static final int SAMPLE_RATE = 48000;
    private static final double AMPLITUDE = 0.063; // approximately -24 dBFS
    private final Handler handler = new Handler(Looper.getMainLooper());
    private AudioTrack track;

    synchronized boolean playTone(double frequency, Channel channel, int durationMs) {
        return play(channel, durationMs, (frame, frames) -> frequency);
    }

    synchronized boolean playSweep(int durationMs) {
        return play(Channel.BOTH, durationMs, (frame, frames) -> {
            double progress = frame / (double) Math.max(1, frames - 1);
            return 100.0 * Math.pow(100.0, progress); // logarithmic 100 Hz to 10 kHz
        });
    }

    private boolean play(Channel channel, int durationMs, Frequency frequency) {
        stop();
        int frames = SAMPLE_RATE * durationMs / 1000;
        short[] samples = new short[frames * 2];
        double phase = 0.0;
        for (int frame = 0; frame < frames; frame++) {
            phase += 2.0 * Math.PI * frequency.at(frame, frames) / SAMPLE_RATE;
            short sample = (short) (Math.sin(phase) * Short.MAX_VALUE * AMPLITUDE);
            samples[frame * 2] = channel == Channel.RIGHT ? 0 : sample;
            samples[frame * 2 + 1] = channel == Channel.LEFT ? 0 : sample;
        }
        try {
            track = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                            .build())
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(samples.length * 2)
                    .build();
            if (track.write(samples, 0, samples.length) != samples.length) {
                stop();
                return false;
            }
            track.play();
            handler.postDelayed(this::stop, durationMs + 150L);
            return true;
        } catch (IllegalArgumentException | IllegalStateException e) {
            stop();
            return false;
        }
    }

    synchronized void stop() {
        handler.removeCallbacksAndMessages(null);
        if (track == null) return;
        try { track.stop(); } catch (IllegalStateException ignored) {}
        track.release();
        track = null;
    }

    private interface Frequency { double at(int frame, int frames); }
}

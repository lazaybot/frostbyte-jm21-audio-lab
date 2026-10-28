package com.frostbyte.theme;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.audiofx.Visualizer;

final class AudioProbe {
    private Visualizer visualizer;
    private volatile byte[] fft;

    boolean start(Context context, Runnable invalidate) {
        stop();
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) return false;
        try {
            visualizer = new Visualizer(0);
            int[] range = Visualizer.getCaptureSizeRange();
            visualizer.setCaptureSize(Math.min(256, range[1]));
            visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override public void onWaveFormDataCapture(Visualizer v, byte[] data, int rate) {}
                @Override public void onFftDataCapture(Visualizer v, byte[] data, int rate) { fft = data.clone(); invalidate.run(); }
            }, Math.min(Visualizer.getMaxCaptureRate() / 4, 4000), false, true);
            visualizer.setEnabled(true); return true;
        } catch (RuntimeException | UnsatisfiedLinkError e) { stop(); return false; }
    }

    byte[] fft() { return fft; }
    void stop() {
        if (visualizer != null) {
            try { visualizer.setEnabled(false); } catch (RuntimeException ignored) {}
            visualizer.release(); visualizer = null;
        }
        fft = null;
    }
}

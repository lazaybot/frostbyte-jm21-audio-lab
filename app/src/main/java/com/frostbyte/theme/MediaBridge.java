package com.frostbyte.theme;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

final class MediaBridge {
    interface Listener { void onMediaChanged(); }
    @SuppressLint("StaticFieldLeak") // Holds only context.getApplicationContext().
    private static volatile MediaBridge instance;
    static MediaBridge get(Context c) {
        if (instance == null) synchronized (MediaBridge.class) {
            if (instance == null) instance = new MediaBridge(c.getApplicationContext());
        }
        return instance;
    }

    private final Context context;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();
    private MediaController controller;
    private String title = "NO ACTIVE SIGNAL", artist = "OPEN A MUSIC APP";
    private Bitmap artwork;
    private boolean playing;

    private final MediaController.Callback callback = new MediaController.Callback() {
        @Override public void onMetadataChanged(MediaMetadata metadata) { update(metadata, controller == null ? null : controller.getPlaybackState()); }
        @Override public void onPlaybackStateChanged(PlaybackState state) { update(controller == null ? null : controller.getMetadata(), state); }
        @Override public void onSessionDestroyed() { refreshSessions(); }
    };

    private MediaBridge(Context context) { this.context = context; }
    void addListener(Listener l) { listeners.addIfAbsent(l); refreshSessions(); }
    void removeListener(Listener l) { listeners.remove(l); }
    String title() { return title; }
    String artist() { return artist; }
    Bitmap artwork() { return artwork; }
    boolean isPlaying() { return playing; }

    void refreshSessions() {
        main.post(() -> {
            try {
                MediaSessionManager msm = context.getSystemService(MediaSessionManager.class);
                ComponentName listener = new ComponentName(context, MediaNotificationListener.class);
                List<MediaController> sessions = msm.getActiveSessions(listener);
                MediaController next = sessions.isEmpty() ? null : sessions.get(0);
                if (controller != next) {
                    if (controller != null) controller.unregisterCallback(callback);
                    controller = next;
                    if (controller != null) controller.registerCallback(callback, main);
                }
                update(controller == null ? null : controller.getMetadata(), controller == null ? null : controller.getPlaybackState());
            } catch (SecurityException ignored) { update(null, null); }
        });
    }

    void previous() { if (controller != null) controller.getTransportControls().skipToPrevious(); }
    void toggle() {
        if (controller == null) return;
        if (playing) controller.getTransportControls().pause(); else controller.getTransportControls().play();
    }
    void next() { if (controller != null) controller.getTransportControls().skipToNext(); }

    private void update(MediaMetadata m, PlaybackState s) {
        if (m == null) {
            title = "NO ACTIVE SIGNAL"; artist = "OPEN A MUSIC APP"; artwork = null;
        } else {
            title = value(m, MediaMetadata.METADATA_KEY_TITLE, "UNKNOWN TRACK");
            artist = value(m, MediaMetadata.METADATA_KEY_ARTIST, value(m, MediaMetadata.METADATA_KEY_ALBUM_ARTIST, "UNKNOWN ARTIST"));
            Bitmap raw = m.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
            if (raw == null) raw = m.getBitmap(MediaMetadata.METADATA_KEY_ART);
            artwork = scale(raw);
        }
        playing = s != null && s.getState() == PlaybackState.STATE_PLAYING;
        for (Listener l : listeners) l.onMediaChanged();
    }

    private static String value(MediaMetadata m, String key, String fallback) {
        String v = m.getString(key); return v == null || v.trim().isEmpty() ? fallback : v;
    }
    private static Bitmap scale(Bitmap b) {
        if (b == null || (b.getWidth() <= 320 && b.getHeight() <= 320)) return b;
        float f = 320f / Math.max(b.getWidth(), b.getHeight());
        return Bitmap.createScaledBitmap(b, Math.max(1, Math.round(b.getWidth() * f)), Math.max(1, Math.round(b.getHeight() * f)), true);
    }
}

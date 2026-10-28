package com.frostbyte.theme;

import android.Manifest;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class MainActivity extends Activity {
    private TextView status;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(28), dp(24), dp(24));
        root.setBackgroundColor(ThemeColors.NAVY);

        TextView title = text("FROSTBYTE // 1984\nSYSTEM CONFIGURATION", 24, ThemeColors.AMBER);
        root.addView(title);
        root.addView(text("Low-power live terminal for FiiO JM21\n750×1334 / Android 13 profile", 14, ThemeColors.FROST), margins(0, 14));
        status = text("", 13, Color.LTGRAY);
        root.addView(status, margins(0, 22));
        root.addView(button("01  ENABLE MEDIA ACCESS", v -> startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))));
        root.addView(button("02  ALLOW OPTIONAL AUDIO FFT", v -> requestAudioPermission()));
        root.addView(button("03  PREVIEW / SET WALLPAPER", v -> openWallpaperPreview()));
        root.addView(button("04  OPEN JM21 AUDIO LAB", v -> startActivity(new Intent(this, AudioLabActivity.class))));
        root.addView(text("LOCK SCREEN NOTE\nChoose “Home and lock screen” in the system preview if offered. Android 13 owns lock-screen playback controls; this wallpaper does not replace them.", 12, 0xFF9BA8B4), margins(0, 24));
        setContentView(root);
    }

    @Override protected void onResume() { super.onResume(); refreshStatus(); }

    private void refreshStatus() {
        boolean media = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners") != null &&
                Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners").contains(getPackageName());
        boolean audio = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        status.setText(getString(R.string.status_format,
                getString(media ? R.string.status_online : R.string.status_permission_required),
                getString(audio ? R.string.status_authorized : R.string.status_optional)));
    }

    private void requestAudioPermission() {
        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1984);
    }

    private void openWallpaperPreview() {
        Intent i = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(this, FrostbyteWallpaperService.class));
        startActivity(i);
    }

    private Button button(String label, android.view.View.OnClickListener action) {
        Button b = new Button(this);
        b.setText(label); b.setTextColor(ThemeColors.NAVY); b.setTextSize(13); b.setAllCaps(false);
        b.setBackgroundColor(ThemeColors.AMBER); b.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        b.setPadding(dp(14), 0, dp(14), 0); b.setOnClickListener(action);
        b.setLayoutParams(margins(0, 10)); return b;
    }

    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setTypeface(android.graphics.Typeface.MONOSPACE); t.setLineSpacing(0, 1.15f); return t;
    }

    private LinearLayout.LayoutParams margins(int top, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(top), 0, dp(bottom)); return p;
    }
    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
}

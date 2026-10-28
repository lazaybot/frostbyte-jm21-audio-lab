package com.frostbyte.theme;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public final class AudioLabActivity extends Activity {
    private final ToneEngine tones = new ToneEngine();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(22), dp(20), dp(28));
        root.setBackgroundColor(ThemeColors.NAVY);

        root.addView(text("JM21 // AUDIO ENGINEERING LAB", 22, ThemeColors.AMBER));
        root.addView(text("DIAGNOSTICS + SAFE SIGNAL TESTS", 13, ThemeColors.FROST), margins(6, 18));
        root.addView(text(AudioDiagnostics.report(this), 12, Color.LTGRAY), margins(0, 18));
        root.addView(text("SIGNAL TESTS", 16, ThemeColors.AMBER));
        root.addView(text("Set the JM21 to a low volume before starting. Tests use approximately -24 dBFS and stop automatically. Do not use them to estimate a safe listening level.", 12, 0xFF9BA8B4), margins(6, 10));
        root.addView(button("LEFT CHANNEL // 1 kHz // 2 s", () -> tone(1000, ToneEngine.Channel.LEFT, 2000)));
        root.addView(button("RIGHT CHANNEL // 1 kHz // 2 s", () -> tone(1000, ToneEngine.Channel.RIGHT, 2000)));
        root.addView(button("STEREO CENTER // 1 kHz // 2 s", () -> tone(1000, ToneEngine.Channel.BOTH, 2000)));
        root.addView(button("LOG SWEEP // 100 Hz–10 kHz // 5 s", this::sweep));
        root.addView(button("STOP SIGNAL", tones::stop));
        root.addView(text("ENGINEERING BOUNDARY\nThe lab reports Android audio-device capabilities and generates controlled PCM signals. It does not modify FiiO firmware, measure analog distortion, certify bit-perfect playback, or expose the internal CS43198 signal path.", 12, 0xFF9BA8B4), margins(18, 0));

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
    }

    @Override protected void onStop() {
        tones.stop();
        super.onStop();
    }

    private void tone(double frequency, ToneEngine.Channel channel, int durationMs) {
        if (!tones.playTone(frequency, channel, durationMs)) failure();
    }

    private void sweep() {
        if (!tones.playSweep(5000)) failure();
    }

    private void failure() {
        Toast.makeText(this, "Android could not open the audio output", Toast.LENGTH_LONG).show();
    }

    private Button button(String label, Runnable action) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(ThemeColors.NAVY);
        button.setTextSize(12);
        button.setAllCaps(false);
        button.setBackgroundColor(ThemeColors.AMBER);
        button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        button.setPadding(dp(12), 0, dp(12), 0);
        button.setOnClickListener(view -> action.run());
        button.setLayoutParams(margins(0, 8));
        return button;
    }

    private TextView text(String value, int size, int color) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        text.setTypeface(android.graphics.Typeface.MONOSPACE);
        text.setLineSpacing(0, 1.15f);
        return text;
    }

    private LinearLayout.LayoutParams margins(int top, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(top), 0, dp(bottom));
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}

package com.frostbyte.theme;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;

import java.util.Arrays;

final class AudioDiagnostics {
    private AudioDiagnostics() {}

    static String report(Context context) {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        StringBuilder report = new StringBuilder();
        report.append("ANDROID ").append(Build.VERSION.RELEASE)
                .append(" / API ").append(Build.VERSION.SDK_INT).append('\n');
        report.append("DEVICE ").append(Build.MANUFACTURER).append(' ')
                .append(Build.MODEL).append('\n');
        report.append("MIXER DEFAULT RATE ")
                .append(value(manager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE), "unknown"))
                .append(" Hz\n");
        report.append("FRAMES PER BUFFER ")
                .append(value(manager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER), "unknown"))
                .append("\n\nOUTPUT DEVICES\n");

        AudioDeviceInfo[] devices = manager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
        if (devices.length == 0) report.append("none reported by Android\n");
        for (AudioDeviceInfo device : devices) {
            report.append("• ").append(typeName(device.getType()));
            CharSequence product = device.getProductName();
            if (product != null && product.length() > 0) report.append(" / ").append(product);
            report.append('\n');
            int[] rates = device.getSampleRates();
            report.append("  declared rates: ")
                    .append(rates.length == 0 ? "unspecified" : Arrays.toString(rates))
                    .append('\n');
            report.append("  max channels: ").append(max(device.getChannelCounts())).append('\n');
        }
        report.append("\nThese values describe Android's reported capabilities. They do not prove the active music app is bit-perfect or reveal the JM21 DAC's internal clock.");
        return report.toString();
    }

    private static String value(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }

    private static int max(int[] values) {
        int max = 0;
        for (int value : values) max = Math.max(max, value);
        return max;
    }

    private static String typeName(int type) {
        switch (type) {
            case AudioDeviceInfo.TYPE_BUILTIN_SPEAKER: return "built-in speaker";
            case AudioDeviceInfo.TYPE_WIRED_HEADPHONES: return "wired headphones";
            case AudioDeviceInfo.TYPE_WIRED_HEADSET: return "wired headset";
            case AudioDeviceInfo.TYPE_USB_DEVICE: return "USB audio device";
            case AudioDeviceInfo.TYPE_USB_HEADSET: return "USB headset";
            case AudioDeviceInfo.TYPE_BLUETOOTH_A2DP: return "Bluetooth A2DP";
            case AudioDeviceInfo.TYPE_HDMI: return "HDMI";
            default: return "Android audio type " + type;
        }
    }
}

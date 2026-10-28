# FROSTBYTE // 1984

Native Android live wallpaper and audio-engineering utility for the FiiO JM21
(Android 13, 750×1334). It renders an amber cyberpunk landscape, clock, battery,
media metadata/artwork, transport controls and an optional audio FFT. Version
0.2.0 also includes a native JM21 Audio Lab.

## JM21 Audio Lab

- Reports Android's default mixer rate and frames per buffer.
- Lists reported output devices, declared sample rates and channel counts.
- Generates low-level left, right and stereo 1 kHz channel checks.
- Generates a logarithmic 100 Hz–10 kHz diagnostic sweep.
- Stops every signal automatically and releases audio when the activity closes.

The diagnostics describe Android's reported capabilities. They do not prove
bit-perfect playback, measure the analog headphone outputs or expose FiiO's
internal CS43198 configuration.

## Why this architecture

- One APK and one wallpaper service; no launcher replacement or permanent foreground service.
- Canvas-only graphics; no video, WebView, Compose runtime, network access, or bundled bitmap assets.
- 15 FPS only while media is playing, 1 FPS while visible and idle, 0 FPS while hidden.
- Artwork is capped at 320 px. FFT capture is optional and released whenever the wallpaper is hidden.
- Media metadata and transport commands use Android's active `MediaSession` APIs after the user enables notification access.
- Up to three installed players are exposed as on-wallpaper shortcuts (FiiO Music, Spotify, Apple Music, TIDAL, Qobuz, or Amazon Music, in that priority order).

## Build

Requirements: JDK 17 and Android SDK 35.

```sh
./gradlew :app:assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

A prebuilt, debug-signed APK is included in the deliverables. Android will identify it as a sideloaded app; no network permission or analytics code is present.

## Install and configure

1. Install with `adb install -r app-debug.apk`, or copy the APK to the JM21 and open it.
2. Open **FROSTBYTE // 1984**.
3. Enable media access. This is needed to read active music sessions and send previous/play/next commands.
4. Audio FFT is optional. Grant microphone/audio permission only if wanted. The visualizer self-disables if FiiO's firmware or the active player blocks global output capture.
5. Tap **Preview / Set Wallpaper** and choose **Home and lock screen** if that choice is present.

## Compatibility boundaries

- Android 13 supports live wallpapers, `MediaSessionManager`, and lock-specific wallpaper flags.
- Android 13's lock-screen controls are rendered by System UI from the music app's media notification. A third-party wallpaper cannot replace or reposition those controls.
- Some players do not publish complete metadata/artwork or support every transport action.
- Global FFT uses Android's `Visualizer(0)` API and requires audio permission. Vendor firmware or individual players may deny capture; the wallpaper then shows a low-cost synthetic activity pattern rather than claiming reactive audio.
- Tapping wallpaper controls is intended for the unlocked home screen. Launchers or OEM builds may reserve wallpaper touch events.
- Music shortcuts are deliberately restricted to known player package IDs so the APK does not enumerate unrelated apps.

## Device test checklist

- [ ] Set on both home and lock screens; verify matching layout and no crop at 750×1334.
- [ ] Enable notification access; test FiiO Music plus each installed streaming app.
- [ ] Verify title, artist, artwork, previous, play/pause, and next.
- [ ] With FFT permission denied: confirm `FFT SAFE` and stable fallback animation.
- [ ] With FFT permission granted: confirm `FFT LIVE` and bars respond to audio; revoke permission if unsupported.
- [ ] Turn screen off for 10 minutes and confirm no abnormal battery drop or warmth.
- [ ] Inspect memory with `adb shell dumpsys meminfo com.frostbyte.theme` while playing and idle.
- [ ] Inspect CPU with `adb shell top -o PID,CPU,RES,ARGS -p $(adb shell pidof com.frostbyte.theme)`.
- [ ] Open JM21 Audio Lab at low system volume and verify left/right routing.
- [ ] Compare reported devices with 3.5 mm, 4.4 mm, Bluetooth and USB outputs.

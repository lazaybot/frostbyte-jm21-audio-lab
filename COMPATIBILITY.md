# FROSTBYTE // 1984 compatibility decision

Checked 2026-10-23 for the FiiO JM21 3 GB / 32 GB model.

## Verified device baseline

FiiO specifies Android 13, a 750×1334 4.7-inch display, Snapdragon 680, 3 GB RAM, about 22 GB usable internal storage, and a 2400 mAh battery for this model. FiiO also warns that the 3 GB device should primarily run music apps and avoid multiple background apps.

Source: https://www.fiio.com/jm21

## Feature decisions

| Requested feature | Decision | Evidence / limitation |
|---|---|---|
| Matching home and lock screen | Implemented as one live wallpaper | Android supports lock-specific wallpapers from API 24. Runtime lock state uses `KeyguardManager`, which is available on Android 13. Final availability of the “Home and lock screen” choice is controlled by FiiO System UI and must be checked on the device. |
| Slowly rotating planet and wireframe landscape | Implemented | Procedural Canvas rendering; no video decoder, network, or large texture allocation. |
| Real metadata and artwork | Implemented with permission gate | Android active media sessions are accessible to an enabled notification listener. Apps that publish incomplete metadata remain incomplete. |
| Previous/play/next | Implemented | Commands are sent to the highest-priority active `MediaController`; unsupported player actions may be ignored by that player. |
| Reactive visualizer | Conditional | Android `Visualizer(0)` supports output-mix FFT but requires audio and modify-audio-settings permissions. Vendor firmware or protected audio may reject it, so failure falls back safely and is not represented as live FFT. |
| Clock and battery | Implemented | Local clock and BatteryManager; no background polling while wallpaper is hidden. |
| Music-app shortcuts | Implemented for known players | Up to three installed known music apps are shown. Wallpaper touch handling is launcher/OEM-dependent and is disabled on the lock screen. |
| Audio-device diagnostics | Implemented | Reports public `AudioManager` and `AudioDeviceInfo` values. These capabilities do not prove the active route or bit-perfect playback. |
| Channel and sweep tests | Implemented | Native 48 kHz PCM at approximately -24 dBFS, with automatic stop. Analog measurements still require external test equipment. |
| Custom lock-screen controls | Not implemented | Android 13 System UI owns lock-screen media controls. A wallpaper cannot safely replace or reposition them. |

Android references:

- Live wallpaper and lock flag: https://developer.android.com/reference/android/app/WallpaperManager
- Active sessions and notification-listener access: https://developer.android.com/reference/android/media/session/MediaSessionManager
- Audio FFT permissions and output mix: https://developer.android.com/reference/android/media/audiofx/Visualizer

## Resource policy

- 15 FPS during active playback; 1 FPS while visible and idle; no frames while hidden.
- No launcher replacement, WebView, Compose, foreground service, network permission, analytics, or bundled media.
- Album art downscaled to at most 320 px.
- Visualizer acquired only while visible and released immediately when hidden.

## Validation status

- Clean APK compilation: passed with Gradle 8.9, Android Gradle Plugin 8.7.3, JDK 17, and Android API 35.
- Android Lint: passed after fixing an API 34-only wallpaper-engine call; no lint issues remain.
- APK signature verification: passed using APK Signature Scheme v2 and the standard Android debug certificate.
- Package inspection: `com.frostbyte.theme`, version `0.1.0`, minSdk 24, targetSdk 35; APK is approximately 25 KB.
- Device behavior: not tested because `adb devices -l` returned no connected Android device.

The APK is API-compatible with Android 13 and passes build, lint, and signature checks. Firmware-specific behavior—especially global FFT capture, the lock-screen wallpaper chooser, and launcher forwarding of wallpaper taps—still requires the checklist in `README.md` on the actual JM21.

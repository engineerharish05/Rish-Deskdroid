# Rish Deskdroid

A landscape Android launcher with a macOS-style frosted-glass look, built from three hand-drawn designs:
home screen, 5 x 3 app drawer, and app settings. Kotlin + Jetpack Compose, no third-party libraries.

> **Status:** source project is configured to build through GitHub Actions. Install and test the generated APK on a device before treating all features as verified.

## Build and install

**Android Studio**
1. Open this folder and let Gradle sync.
2. Connect a phone with USB debugging enabled, or start an emulator (API 26+), and press Run.
3. Press the Home button. Android asks which launcher to use. Choose **Rish Deskdroid** > Always.

**Command line**
```
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
Needs JDK 17 and the Android SDK (platform 35). Set `sdk.dir` in `local.properties` or `ANDROID_HOME`.

**Build without local setup:** use the workflow in `.github/workflows/build.yml`, then download the APK from the successful run's Artifacts.

To switch back to your old launcher: tap the red **Exit** tile in the dock, or go to Settings > Apps > Default apps > Home app.

## What is where

| Screen | What it does |
| --- | --- |
| **Home** | Status bar, widgets, glass dock. Swipe up (or tap the grid tile) to open the drawer. |
| **App drawer** | Every app in a 5 x 3 grid, swipe sideways for more pages, page dots at the bottom. Long-press an app for Add to dock, App info, Uninstall. Tap empty space or press Back to close. |
| **App settings** | Widgets, OTG Shortcut, Keyboard shortcuts, Glass Opacity, Wallpaper change. Press Back to leave. |

**Dock, left to right:** glass settings handle (quick opacity slider) · Mobile settings · Chrome, YouTube,
Google, GPT (whichever are installed) · + add an app · OTG (only while a USB device is connected) ·
App drawer · App settings · Exit (opens Android's launcher picker).
Long-press a dock app to remove it. The dock holds up to 6 apps.

**Status bar:** carrier and time on the left. Bluetooth, Wi-Fi, signal bars and battery % on the right;
Bluetooth, Wi-Fi and signal only appear when they are on/available. Android's own status bar is hidden;
swipe down from the top edge to peek at it and pull down notifications.

**Keyboard shortcuts** (Settings > Keyboard shortcuts, off by default): Ctrl+D drawer, Ctrl+, settings,
Ctrl+H home, Ctrl+1 to 9 launch the dock apps, Esc go back.

## Design and behavior notes

- **OTG Shortcut:** shows a dock tile only while a USB device is attached; it opens the system file picker.
- **Exit:** opens Android's default Home app chooser.
- **Navigation bar:** Android provides its own navigation controls in landscape.
- **Glass surfaces:** translucent white with a bright edge; wallpaper blur is used behind the drawer and settings on Android 12+.
- **Landscape only**, matching the design.
- **Wallpaper** is stored inside the launcher, not set as the system wallpaper.
- **Widgets:** the + in Settings lists widgets available on the device. Android asks permission the first time.

## Project layout

```
app/src/main/java/com/rishdeskdroid/app/
  MainActivity.kt
  data/   LauncherPrefs, Apps, StatusController, WidgetController, WallpaperStore
  ui/     LauncherRoot, HomeScreen, DrawerScreen, SettingsScreen, StatusBar,
          Glass, Icons, Wallpaper, WidgetView, Dialogs
```

Permissions: network state, Bluetooth state on Android 11 and older, and request-delete-packages for the Uninstall menu item.

## License

Copyright (c) 2026 Harish P.

The original project code in Rish Deskdroid is licensed under the **GNU General Public License v3.0 only (GPL-3.0-only)**. See the [LICENSE](LICENSE) file for the complete terms.

You may use, study, modify, and redistribute covered code in accordance with GPL-3.0. If you distribute covered copies or modified versions, you must comply with the license, including its applicable source-code and notice requirements. This license does not grant permission to use the Rish Deskdroid name, logo, or other branding as an endorsement.

Third-party libraries and other components remain subject to their respective licenses. This project was developed with AI assistance; licensing this project does not override any rights or obligations that may apply to third-party material.

# Glass Launcher

A landscape Android launcher with a macOS-style frosted-glass look, built from three hand-drawn designs:
home screen, 5 x 3 app drawer, and app settings. Kotlin + Jetpack Compose, no third-party libraries.

> **Status:** written from the designs but **not yet compiled or run on a device**. Build it once
> (below) and send back any errors or anything that looks off.

## Build and install

**Android Studio (easiest)**
1. File > Open > select this folder. Let Gradle sync (it downloads the Android SDK pieces it needs).
2. Plug in a phone with USB debugging on, or start an emulator (API 26+), and press Run.
3. Press the Home button. Android asks which launcher to use. Choose **Glass Launcher** > Always.

**Command line**
```
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
Needs JDK 17 and the Android SDK (platform 35). Set `sdk.dir` in `local.properties` or `ANDROID_HOME`.

**No local setup:** push this folder to a GitHub repo. The included workflow
(`.github/workflows/build.yml`) builds the APK; download it from the run's Artifacts.

To switch back to your old launcher: tap the red **Exit** tile in the dock, or go to
Settings > Apps > Default apps > Home app.

## What is where

| Screen | What it does |
| --- | --- |
| **Home** | Status bar, widgets, glass dock. Swipe up (or tap the grid tile) to open the drawer. |
| **App drawer** | Every app in a 5 x 3 grid, swipe sideways for more pages, page dots at the bottom. Long-press an app for Add to dock, App info, Uninstall. Tap empty space or press Back to close. |
| **App settings** | Widgets, OTG Shortcut, Keyboard shortcuts, Glass Opacity, Wallpaper change. Press Back to leave. |

**Dock, left to right:** Glass settings handle (quick opacity slider) · Mobile settings · Chrome, YouTube,
Google, GPT (whichever are installed) · + add an app · OTG (only while a USB device is connected) ·
App drawer · App settings · Exit (opens Android's launcher picker).
Long-press a dock app to remove it. The dock holds up to 6 apps.

**Status bar:** carrier and time on the left. Bluetooth, Wi-Fi, signal bars and battery % on the right;
Bluetooth, Wi-Fi and signal only appear when they are on/available. Android's own status bar is hidden;
swipe down from the top edge to peek at it and pull down notifications.

**Keyboard shortcuts** (Settings > Keyboard shortcuts, off by default): Ctrl+D drawer, Ctrl+, settings,
Ctrl+H home, Ctrl+1 to 9 launch the dock apps, Esc go back.

## Decisions I made where the sketches were open

- **OTG Shortcut:** shows a dock tile only while a USB device is attached; it opens the system file picker,
  where a USB drive appears in the sidebar.
- **Exit:** a launcher can't really "exit", so this opens the default Home app chooser.
- **Navigation bar:** your sketch's right-edge back / home / recents is Android's own 3-button navigation in
  landscape, so the launcher does not draw it. (Gesture navigation works too.)
- **Glass:** surfaces are translucent white with a bright edge, and the wallpaper blurs behind the drawer and
  settings on Android 12+. Compose can't blur what is directly behind a panel, so the dock itself is
  translucent rather than truly frosted.
- **Landscape only**, as in the designs.
- **Wallpaper** is stored inside the launcher, not set as the system wallpaper.
- **Widgets:** the + in Settings lists every widget on the device. Android asks permission the first time.
  Tap a widget chip in Settings to remove it.

## Project layout

```
app/src/main/java/com/glasslauncher/app/
  MainActivity.kt            window setup, key shortcuts, USB + package receivers
  data/   LauncherPrefs, Apps (app list, icons, launching), StatusController,
          WidgetController, WallpaperStore
  ui/     LauncherRoot, HomeScreen, DrawerScreen, SettingsScreen, StatusBar,
          Glass (glass surface, tiles, switch), Icons, Wallpaper, WidgetView, Dialogs
```

Permissions: network state (Wi-Fi/cellular icons), Bluetooth state on Android 11 and older, and
request-delete-packages (the Uninstall menu item). No runtime permission prompts.

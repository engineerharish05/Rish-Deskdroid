# Rish Deskdroid

A simple Android launcher built with Kotlin and Jetpack Compose.

## Essential features

- Selectable Android Home launcher
- Responsive home screen for portrait and landscape layouts
- Installed-app icons and quick-launch grid
- Searchable app drawer
- Live clock and date
- Shortcut to Android Settings
- USB keyboard shortcuts while the launcher has focus:
  - Ctrl+Space or F1: open/close app drawer
  - Esc: close app drawer
- No account, analytics, ads, or network permission

## Compatibility

- Minimum Android version: Android 12 (API 31)
- Uses Android's normal app launching and system Settings intents.
- This is a simple launcher, not a full desktop operating system. It cannot force other apps to rotate or run in floating windows.
- QUERY_ALL_PACKAGES is used to display installed launchable apps.
- Intended for personal sideloading and experimentation.

## Build the APK using your phone

1. Open the Actions tab in this repository.
2. Select "Build Rish Desktop APK" and run the workflow, or wait for the build triggered by a push to main.
3. Open the completed workflow run and download the rish-desktop-debug-apk artifact.
4. Extract the artifact ZIP and install app-debug.apk.
5. If Android asks, allow the browser or file manager to install unknown apps.

## Set as your Home app

1. Install and open Rish Deskdroid.
2. Press Home and choose Rish Deskdroid when Android asks which Home app to use.
3. To switch back, open Android Settings > Apps > Default apps > Home app and select the previous launcher.

## Build locally

Requires JDK 17, Android SDK 35, and Gradle 8.11.1.

Run: gradle assembleDebug

The APK is generated at app/build/outputs/apk/debug/app-debug.apk.

## License

MIT.

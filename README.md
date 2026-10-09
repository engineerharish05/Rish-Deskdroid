# Rish Desktop

A free, open-source Android desktop-style home launcher built with Kotlin and Jetpack Compose.

## Features
- Landscape-locked home workspace
- Installed-app grid and searchable app drawer
- Quick-launch dock
- USB keyboard shortcuts while the launcher has focus:
  - Ctrl+Space or F1: open/close app drawer
  - Esc: close app drawer
- No account, analytics, ads, or network permission

## Compatibility notes
- Intended for Android 8.0+; builds with Android SDK 35.
- This is a launcher, not a full desktop OS. It cannot force other apps to rotate or make them resizable.
- Floating windows depend on Realme firmware and Android window-management support.
- QUERY_ALL_PACKAGES is used to list installed launchable apps.
- This repository is intended for personal sideloading and experimentation.

## Build the APK using only your phone
1. Open the Actions tab in this repository.
2. Select "Build Rish Desktop APK" and run the workflow, or push a commit to main.
3. Open the completed workflow run and download the `rish-desktop-debug-apk` artifact.
4. Extract the downloaded artifact ZIP on your phone.
5. Install `app-debug.apk`. If Android asks, allow your browser or file manager to install unknown apps.

## Set as your home app
1. Install the APK and press Home.
2. Choose Rish Desktop when Android asks which home app to use.
3. To revert, open Settings > Apps > Default apps > Home app and select the Realme launcher.

## Build locally
Requires JDK 17, Android SDK 35, and Gradle 8.11.1:
```sh
gradle assembleDebug
```
The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## License
MIT.
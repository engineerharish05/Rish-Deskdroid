# Rish Deskdroid

A native Android launcher built with Kotlin and Jetpack Compose, styled around the Rish Deskdroid landscape mockup.

## Features
- The Rish Deskdroid launcher activity is locked to landscape orientation.
- Can be selected as the Android Home launcher or opened like a regular app.
- Responsive home surface with live clock/date and a blue, purple, and pink gradient.
- App library discovers launchable apps installed on the device and opens them.
- Search filters the app library.
- Glass-style dock; long-press an app to pin/unpin it.
- Dock selections persist using Android SharedPreferences and can be edited from the Dock action.
- Quick settings panel opens the real Android Wi-Fi, Bluetooth, Display, and System settings screens.
- Back button returns from the app library to the home surface.

## Build a debug APK
GitHub Actions builds the debug APK on every push to `main` and pull requests, and can also be started manually.

1. Open the repository's **Actions** tab.
2. Select **Build Rish Deskdroid Debug APK**.
3. Tap **Run workflow** if you want to start a build manually.
4. Open the completed run and download the `rish-deskdroid-debug-apk` artifact.
5. Extract the ZIP to get `app-debug.apk`, then install it on your Android device.

The build uses JDK 17, Android Gradle Plugin 8.7.3, Gradle 8.11.1, and Android SDK 35.

## Install and choose as Home app
Install the APK, press Home, and select **Rish Deskdroid**. To switch back, open Android Settings → Apps → Default apps → Home app.

## Important Android orientation limitation
Rish Deskdroid itself stays in landscape. When you open another app, Android gives that app control of its own orientation; a normal launcher cannot force every other app to remain landscape. Apps that support landscape should rotate accordingly. To force landscape system-wide for apps that do not support it, a separate Android-level solution with elevated permissions or device-specific settings would be needed.

## Platform limitations
Android restricts third-party apps from toggling protected Wi-Fi/Bluetooth/system settings directly, so the quick panel opens the corresponding Settings screens. The launcher uses `QUERY_ALL_PACKAGES` to list apps installed on the device. This source has not been device-tested yet; verify the compilation result in GitHub Actions.

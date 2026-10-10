package com.glasslauncher.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.glasslauncher.app.data.AppInfo
import com.glasslauncher.app.data.LauncherPrefs
import com.glasslauncher.app.data.StatusState
import com.glasslauncher.app.data.WidgetController

enum class Screen { Home, Drawer, Settings }

/** Wallpaper, the three screens, and the status bar that sits on top of all of them. */
@Composable
fun LauncherRoot(
    prefs: LauncherPrefs,
    apps: List<AppInfo>,
    widgets: WidgetController,
    status: StatusState,
    screen: Screen,
    otgConnected: Boolean,
    onScreenChange: (Screen) -> Unit,
) {
    BackHandler(enabled = screen != Screen.Home) { onScreenChange(Screen.Home) }

    val sideAndBottom = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
    val sides = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)

    Box(Modifier.fillMaxSize().background(Color(0xFF14163A))) {
        Wallpaper(prefs, blurred = screen != Screen.Home)

        // Dim the blurred wallpaper behind the drawer and settings
        AnimatedVisibility(visible = screen != Screen.Home, enter = fadeIn(), exit = fadeOut()) {
            Box(Modifier.fillMaxSize().background(Color(0xFF0A0C20).copy(alpha = 0.5f)))
        }

        Box(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(sideAndBottom)
                .padding(top = StatusBarHeight),
        ) {
            AnimatedVisibility(visible = screen == Screen.Home, enter = fadeIn(), exit = fadeOut()) {
                HomeScreen(
                    prefs = prefs,
                    apps = apps,
                    widgets = widgets,
                    otgConnected = otgConnected,
                    onOpenDrawer = { onScreenChange(Screen.Drawer) },
                    onOpenSettings = { onScreenChange(Screen.Settings) },
                )
            }
            AnimatedVisibility(
                visible = screen == Screen.Drawer,
                enter = fadeIn() + slideInVertically { it / 10 },
                exit = fadeOut() + slideOutVertically { it / 10 },
            ) {
                DrawerScreen(
                    prefs = prefs,
                    apps = apps,
                    onClose = { onScreenChange(Screen.Home) },
                )
            }
            AnimatedVisibility(
                visible = screen == Screen.Settings,
                enter = fadeIn() + slideInVertically { it / 10 },
                exit = fadeOut() + slideOutVertically { it / 10 },
            ) {
                SettingsScreen(prefs = prefs, widgets = widgets)
            }
        }

        StatusBar(
            status = status,
            opacity = prefs.glassOpacity,
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(sides),
        )
    }
}

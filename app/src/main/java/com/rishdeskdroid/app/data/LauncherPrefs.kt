package com.rishdeskdroid.app.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** One app shortcut on the home screen. */
data class HomeShortcut(val slot: Int, val packageName: String)

/**
 * All launcher settings, backed by SharedPreferences and exposed as Compose state so the UI
 * updates the moment a value changes.
 */
class LauncherPrefs(context: Context) {

    private val sp = context.applicationContext
        .getSharedPreferences("glass_launcher", Context.MODE_PRIVATE)

    /** Settings > Widgets ON/OFF */
    var widgetsEnabled by mutableStateOf(sp.getBoolean(K_WIDGETS, true))
        private set

    /** Settings > OTG Shortcut ON/OFF */
    var otgEnabled by mutableStateOf(sp.getBoolean(K_OTG, true))
        private set

    /** Settings > Keyboard shortcuts ON/OFF */
    var keyboardEnabled by mutableStateOf(sp.getBoolean(K_KEYS, false))
        private set

    /** Settings > Glass Opacity, 0f..1f */
    var glassOpacity by mutableFloatStateOf(sp.getFloat(K_GLASS, 0.6f))
        private set

    /** Bumped whenever the wallpaper file changes so the UI reloads it. */
    var wallpaperVersion by mutableIntStateOf(0)
        private set

    /** AppWidget ids placed on the home screen. */
    var widgetIds by mutableStateOf(loadWidgetIds())
        private set

    /** Package names pinned in the dock, or null to use the default dock. */
    var dockPackages by mutableStateOf<List<String>?>(loadDock())
        private set

    /** App shortcuts on the home screen: package name plus grid slot (page * 20 + row * 5 + column). */
    var homeShortcuts by mutableStateOf(loadHomeShortcuts())
        private set

    fun updateWidgetsEnabled(value: Boolean) {
        widgetsEnabled = value
        sp.edit().putBoolean(K_WIDGETS, value).apply()
    }

    fun updateOtgEnabled(value: Boolean) {
        otgEnabled = value
        sp.edit().putBoolean(K_OTG, value).apply()
    }

    fun updateKeyboardEnabled(value: Boolean) {
        keyboardEnabled = value
        sp.edit().putBoolean(K_KEYS, value).apply()
    }

    fun updateGlassOpacity(value: Float) {
        val v = value.coerceIn(0f, 1f)
        glassOpacity = v
        sp.edit().putFloat(K_GLASS, v).apply()
    }

    fun bumpWallpaper() {
        wallpaperVersion += 1
    }

    fun updateWidgetIds(ids: List<Int>) {
        widgetIds = ids
        sp.edit().putString(K_WIDGET_IDS, ids.joinToString(",")).apply()
    }

    fun updateDockPackages(packages: List<String>) {
        dockPackages = packages
        sp.edit().putString(K_DOCK, packages.joinToString("|")).apply()
    }

    fun updateHomeShortcuts(list: List<HomeShortcut>) {
        homeShortcuts = list
        sp.edit().putString(K_HOME, list.joinToString("|") { "${it.slot}:${it.packageName}" }).apply()
    }

    private fun loadHomeShortcuts(): List<HomeShortcut> {
        val seenSlots = HashSet<Int>()
        val seenPackages = HashSet<String>()
        return sp.getString(K_HOME, "")
            .orEmpty()
            .split("|")
            .mapNotNull { entry ->
                val slot = entry.substringBefore(':', "").toIntOrNull() ?: return@mapNotNull null
                val pkg = entry.substringAfter(':', "").trim()
                if (slot < 0 || slot >= HOME_PER_PAGE * MAX_HOME_PAGES || pkg.isEmpty()) null else HomeShortcut(slot, pkg)
            }
            .filter { seenSlots.add(it.slot) && seenPackages.add(it.packageName) }
    }

    private fun loadWidgetIds(): List<Int> =
        sp.getString(K_WIDGET_IDS, "")
            .orEmpty()
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }

    private fun loadDock(): List<String>? {
        val raw = sp.getString(K_DOCK, null) ?: return null
        return raw.split("|").filter { it.isNotBlank() }
    }

    private companion object {
        const val K_WIDGETS = "widgets_enabled"
        const val K_OTG = "otg_enabled"
        const val K_KEYS = "keyboard_shortcuts"
        const val K_GLASS = "glass_opacity"
        const val K_WIDGET_IDS = "widget_ids"
        const val K_DOCK = "dock_packages"
        const val K_HOME = "home_shortcuts"
    }
}

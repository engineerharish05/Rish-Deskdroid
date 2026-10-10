package com.rishdeskdroid.app.data

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import android.view.Surface
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.util.concurrent.ConcurrentHashMap

/** One launchable app. */
data class AppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
) {
    val key: String get() = "$packageName/$activityName"
    val component: ComponentName get() = ComponentName(packageName, activityName)
}

/** Default dock apps, in the order from the design. Only the ones installed are shown. */
val DEFAULT_DOCK = listOf(
    "com.android.chrome",
    "com.google.android.youtube",
    "com.google.android.googlequicksearchbox",
    "com.openai.chatgpt",
)

const val MAX_DOCK_APPS = 6

/** Loads every app that has a launcher icon. */
class AppRepository(private val context: Context) {

    var apps by mutableStateOf<List<AppInfo>>(emptyList())
        private set

    private val handler = Handler(Looper.getMainLooper())

    fun reload() {
        Thread {
            val list = query()
            handler.post {
                IconLoader.clear()
                apps = list
            }
        }.start()
    }

    private fun query(): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        val resolved = pm.queryIntentActivities(intent, 0)
        return resolved
            .filter { it.activityInfo.packageName != context.packageName }
            .map {
                AppInfo(
                    label = it.loadLabel(pm).toString(),
                    packageName = it.activityInfo.packageName,
                    activityName = it.activityInfo.name,
                )
            }
            .distinctBy { it.key }
            .sortedBy { it.label.lowercase() }
    }
}

/** The apps currently pinned in the dock (resolved against what is installed). */
fun dockApps(prefs: LauncherPrefs, apps: List<AppInfo>): List<AppInfo> =
    (prefs.dockPackages ?: DEFAULT_DOCK).mapNotNull { pkg ->
        apps.firstOrNull { it.packageName == pkg }
    }

/** Returns false if the dock is full. */
fun addToDock(prefs: LauncherPrefs, apps: List<AppInfo>, app: AppInfo): Boolean {
    val current = dockApps(prefs, apps).map { it.packageName }
    if (app.packageName in current) return true
    if (current.size >= MAX_DOCK_APPS) return false
    prefs.updateDockPackages(current + app.packageName)
    return true
}

fun removeFromDock(prefs: LauncherPrefs, apps: List<AppInfo>, app: AppInfo) {
    val current = dockApps(prefs, apps).map { it.packageName }
    prefs.updateDockPackages(current - app.packageName)
}

const val HOME_COLUMNS = 5
const val HOME_ROWS = 4
const val HOME_PER_PAGE = HOME_COLUMNS * HOME_ROWS
const val MAX_HOME_PAGES = 10

enum class AddToHomeResult { Added, AlreadyOnHome, Unavailable, Full }

/** Puts the app in the first free home-screen cell (a new page opens when a page is full). */
fun addToHome(prefs: LauncherPrefs, apps: List<AppInfo>, app: AppInfo): AddToHomeResult {
    if (apps.none { it.packageName == app.packageName }) return AddToHomeResult.Unavailable
    val current = prefs.homeShortcuts
    if (current.any { it.packageName == app.packageName }) return AddToHomeResult.AlreadyOnHome
    val used = current.map { it.slot }.toSet()
    val slot = (0 until HOME_PER_PAGE * MAX_HOME_PAGES).firstOrNull { it !in used }
        ?: return AddToHomeResult.Full
    prefs.updateHomeShortcuts(current + HomeShortcut(slot, app.packageName))
    return AddToHomeResult.Added
}

/** Removes only the home shortcut. The app stays installed and stays in the drawer. */
fun removeFromHome(prefs: LauncherPrefs, packageName: String) {
    prefs.updateHomeShortcuts(prefs.homeShortcuts.filter { it.packageName != packageName })
}

/** Forgets shortcuts whose app is no longer installed. */
fun pruneHomeShortcuts(prefs: LauncherPrefs, pm: PackageManager) {
    val kept = prefs.homeShortcuts.filter {
        try {
            pm.getApplicationInfo(it.packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    if (kept.size != prefs.homeShortcuts.size) prefs.updateHomeShortcuts(kept)
}

class LoadedIcon(val bitmap: ImageBitmap, val adaptive: Boolean)

/**
 * Renders app icons to bitmaps. Adaptive icons are drawn as their full 108dp layers and cropped
 * to the visible 72dp area so the UI can clip them to the launcher's own rounded-square shape.
 */
object IconLoader {
    private val cache = ConcurrentHashMap<String, LoadedIcon>()

    fun peek(app: AppInfo): LoadedIcon? = cache[app.key]

    fun clear() = cache.clear()

    fun load(pm: PackageManager, app: AppInfo): LoadedIcon {
        cache[app.key]?.let { return it }

        val drawable = try {
            pm.getActivityIcon(app.component)
        } catch (e: Exception) {
            pm.defaultActivityIcon
        }

        val px = 192
        val bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        var adaptive = false

        if (drawable is AdaptiveIconDrawable) {
            adaptive = true
            val full = px * 108 / 72
            val offset = (full - px) / 2
            canvas.translate(-offset.toFloat(), -offset.toFloat())
            drawable.background?.let {
                it.setBounds(0, 0, full, full)
                it.draw(canvas)
            }
            drawable.foreground?.let {
                it.setBounds(0, 0, full, full)
                it.draw(canvas)
            }
        } else {
            drawable.setBounds(0, 0, px, px)
            drawable.draw(canvas)
        }

        val loaded = LoadedIcon(bitmap.asImageBitmap(), adaptive)
        cache[app.key] = loaded
        return loaded
    }
}

private fun safeStart(context: Context, intent: Intent, errorMessage: String) {
    try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    }
}

fun launchApp(context: Context, app: AppInfo) {
    if (Settings.System.canWrite(context)) {
        lockLandscape(context)
    } else {
        val prefs = context.getSharedPreferences("rish_deskdroid", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("asked_write_settings", false)) {
            prefs.edit().putBoolean("asked_write_settings", true).apply()
            Toast.makeText(
                context,
                "Allow \"Modify system settings\" for Rish Deskdroid, then tap the app again",
                Toast.LENGTH_LONG,
            ).show()
            safeStart(
                context,
                Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:${context.packageName}")),
                "Can't open settings",
            )
            return
        }
    }
    val intent = Intent(Intent.ACTION_MAIN)
        .addCategory(Intent.CATEGORY_LAUNCHER)
        .setComponent(app.component)
        .addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
    safeStart(context, intent, "Can't open ${app.label}")
}

@Suppress("DEPRECATION")
private fun lockLandscape(context: Context) {
    try {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val rotation =
            if (wm.defaultDisplay.rotation == Surface.ROTATION_270) Surface.ROTATION_270 else Surface.ROTATION_90
        val resolver = context.contentResolver
        Settings.System.putInt(resolver, Settings.System.ACCELEROMETER_ROTATION, 0)
        Settings.System.putInt(resolver, Settings.System.USER_ROTATION, rotation)
    } catch (e: Exception) {
        // Not allowed on this device; the app just opens in its default orientation.
    }
}

fun openAppInfo(context: Context, app: AppInfo) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.packageName}"))
    safeStart(context, intent, "Can't open app info")
}

fun uninstallApp(context: Context, app: AppInfo) {
    val intent = Intent(Intent.ACTION_DELETE, Uri.parse("package:${app.packageName}"))
    safeStart(context, intent, "Can't uninstall ${app.label}")
}

fun openMobileSettings(context: Context) {
    safeStart(context, Intent(Settings.ACTION_SETTINGS), "Can't open settings")
}

/** The "Exit" tile: opens Android's default-home picker so the user can switch launcher. */
fun openLauncherChooser(context: Context) {
    try {
        context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        openMobileSettings(context)
    }
}

/**
 * The OTG tile. Android has no public "OTG settings" page, so this tries the system storage
 * screens (where a USB drive is listed and can be opened or ejected) and only then falls back to
 * the system file picker. Each step is checked before launching and failures are handled.
 */
fun openOtgDestination(context: Context) {
    val pm = context.packageManager
    val candidates = listOf(
        Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS),
        Intent(Settings.ACTION_MEMORY_CARD_SETTINGS),
    )
    for (intent in candidates) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(pm) == null) continue
        try {
            context.startActivity(intent)
            return
        } catch (e: ActivityNotFoundException) {
            // try the next destination
        } catch (e: SecurityException) {
            // try the next destination
        }
    }
    openOtgStorage(context)
}

/** The OTG tile fallback: opens the system file picker, where a connected USB drive appears in the sidebar. */
fun openOtgStorage(context: Context) {
    safeStart(context, Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), "No file manager available")
}

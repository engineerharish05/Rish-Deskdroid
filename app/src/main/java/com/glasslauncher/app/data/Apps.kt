package com.glasslauncher.app.data

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
    val intent = Intent(Intent.ACTION_MAIN)
        .addCategory(Intent.CATEGORY_LAUNCHER)
        .setComponent(app.component)
        .addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
    safeStart(context, intent, "Can't open ${app.label}")
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

/** The OTG tile: opens the system file picker, where a connected USB drive appears in the sidebar. */
fun openOtgStorage(context: Context) {
    safeStart(context, Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), "No file manager available")
}

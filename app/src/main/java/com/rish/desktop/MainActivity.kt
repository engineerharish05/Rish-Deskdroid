package com.rish.desktop

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BgTop = Color(0xFF10182B)
private val BgBottom = Color(0xFF070B14)
private val Panel = Color(0xCC182238)
private val Accent = Color(0xFF8DE0FF)
private val TextMain = Color(0xFFF2F6FF)
private val TextMuted = Color(0xFFAAB8D0)

private data class LaunchableApp(
    val label: String,
    val packageName: String,
    val icon: Drawable
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(10, 15, 28)
        window.navigationBarColor = android.graphics.Color.rgb(10, 15, 28)
        setContent { RishDesktopApp { openSettings() } }
    }

    private fun openSettings() {
        runCatching { startActivity(Intent(android.provider.Settings.ACTION_SETTINGS)) }
    }
}

@Composable
private fun RishDesktopApp(onOpenSettings: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val pm = context.packageManager
    var showDrawer by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var apps by remember { mutableStateOf(emptyList<LaunchableApp>()) }

    fun loadApps() {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        apps = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .mapNotNull { info ->
                val activity = info.activityInfo ?: return@mapNotNull null
                if (activity.packageName == context.packageName) return@mapNotNull null
                LaunchableApp(info.loadLabel(pm).toString(), activity.packageName, info.loadIcon(pm))
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    LaunchedEffect(Unit) { loadApps() }
    BackHandler(showDrawer) { showDrawer = false; query = "" }
    val filtered = remember(apps, query) {
        apps.filter { it.label.contains(query, ignoreCase = true) }
    }

    MaterialTheme(colorScheme = darkColorScheme(
        background = BgBottom, surface = Panel, primary = Accent,
        onBackground = TextMain, onSurface = TextMain
    )) {
        Box(
            Modifier.fillMaxSize()
                .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when {
                        event.key == Key.Escape && showDrawer -> {
                            showDrawer = false; query = ""; true
                        }
                        event.key == Key.F1 -> {
                            showDrawer = !showDrawer
                            if (!showDrawer) query = ""
                            true
                        }
                        event.key == Key.Spacebar && event.isCtrlPressed -> {
                            showDrawer = !showDrawer
                            if (!showDrawer) query = ""
                            true
                        }
                        else -> false
                    }
                }
        ) {
            Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 14.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Accent), contentAlignment = Alignment.Center) {
                            Text("R", color = Color(0xFF07111D), fontWeight = FontWeight.Black, fontSize = 22.sp)
                        }
                        Column {
                            Text("RISH DESKTOP", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            Text("A focused space for your work", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(SimpleDateFormat("EEE, dd MMM  •  HH:mm", Locale.getDefault()).format(Date()), color = TextMuted, fontSize = 11.sp)
                        Icon(Icons.Default.Wifi, contentDescription = "Network", tint = TextMuted, modifier = Modifier.size(17.dp))
                        Icon(Icons.Default.BatteryFull, contentDescription = "Battery", tint = Accent, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(Modifier.height(18.dp))
                Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column(Modifier.weight(1.15f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("WORKSPACE", color = TextMuted, fontSize = 10.sp, letterSpacing = 2.sp)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DesktopTile("All apps", "Search and launch apps", Icons.Default.Apps, Modifier.weight(1f)) { showDrawer = true }
                            DesktopTile("Settings", "Configure your phone", Icons.Default.Settings, Modifier.weight(1f), onOpenSettings)
                        }
                        Column(
                            Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(18.dp)).background(Panel).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("KEYBOARD SHORTCUTS", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            ShortcutLine("Ctrl + Space", "Open / close app drawer")
                            ShortcutLine("F1", "Open / close app drawer")
                            ShortcutLine("Esc", "Close app drawer")
                            Spacer(Modifier.height(2.dp))
                            Text("Shortcuts work while this launcher has focus.", color = TextMuted, fontSize = 10.sp)
                        }
                    }
                    Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("QUICK LAUNCH", color = TextMuted, fontSize = 10.sp, letterSpacing = 2.sp)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp)).background(Panel).padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            items(apps.take(6), key = { it.packageName }) { app ->
                                AppIcon(app) { launchApp(context, app.packageName) }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xF21B2941)).padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("R", color = Accent, fontWeight = FontWeight.Black, fontSize = 17.sp)
                        Text("Desktop", color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        DockButton("Apps") { showDrawer = true }
                        DockButton("Settings") { onOpenSettings() }
                    }
                    Text("Ctrl + Space  ·  F1  ·  Esc", color = TextMuted, fontSize = 9.sp)
                }
            }

            if (showDrawer) {
                AppDrawer(
                    apps = filtered, query = query, onQuery = { query = it },
                    onDismiss = { showDrawer = false; query = "" },
                    onLaunch = { pkg -> showDrawer = false; query = ""; launchApp(context, pkg) }
                )
            }
        }
    }
}

@Composable
private fun DesktopTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(Panel).clickable { onClick() }.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Accent, modifier = Modifier.size(23.dp))
        Text(title, color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Text(subtitle, color = TextMuted, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ShortcutLine(keys: String, description: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(keys, color = Accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(105.dp))
        Text(description, color = TextMuted, fontSize = 11.sp)
    }
}

@Composable
private fun DockButton(label: String, onClick: () -> Unit) {
    Text(label, color = TextMain, fontSize = 10.sp,
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onClick() }.padding(horizontal = 10.dp, vertical = 7.dp))
}

@Composable
private fun AppIcon(app: LaunchableApp, onClick: () -> Unit) {
    val bitmap = remember(app.icon) { app.icon.toBitmap(width = 96, height = 96).asImageBitmap() }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onClick() }.padding(vertical = 8.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Image(bitmap, contentDescription = app.label, modifier = Modifier.size(32.dp))
        Text(app.label, color = TextMain, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AppDrawer(
    apps: List<LaunchableApp>,
    query: String,
    onQuery: (String) -> Unit,
    onDismiss: () -> Unit,
    onLaunch: (String) -> Unit
) {
    val focus = remember { FocusRequester() }
    Box(Modifier.fillMaxSize().background(Color(0xEE080D18)).padding(18.dp)) {
        Column(Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)).background(Color(0xFF121C2D)).padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("APPLICATIONS", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 2.sp)
                    Text("${apps.size} apps · type to search", color = TextMuted, fontSize = 10.sp)
                }
                Text("ESC  Close", color = Accent, fontSize = 11.sp, modifier = Modifier.clickable { onDismiss() }.padding(8.dp))
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(11.dp)).background(Color(0xFF202D43)).padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Accent)
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = query, onValueChange = onQuery, singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = TextMain, fontSize = 14.sp),
                    cursorBrush = Brush.horizontalGradient(listOf(Accent, Accent)),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier.fillMaxWidth().focusRequester(focus),
                    decorationBox = { inner ->
                        Box {
                            if (query.isEmpty()) Text("Search installed apps…", color = TextMuted, fontSize = 13.sp)
                            inner()
                        }
                    }
                )
            }
            Spacer(Modifier.height(10.dp))
            if (apps.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No matching apps", color = TextMuted) }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 82.dp),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(apps, key = { it.packageName }) { app -> AppIcon(app) { onLaunch(app.packageName) } }
                }
            }
        }
    }
}

private fun launchApp(context: Context, packageName: String) {
    runCatching {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}

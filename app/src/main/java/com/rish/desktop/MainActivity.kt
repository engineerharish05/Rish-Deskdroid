package com.rish.desktop

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Sky = Color(0xFF9DE7FF)
private val TextPrimary = Color(0xFFF5F8FF)
private val TextMuted = Color(0xFFB8C7DB)
private val Glass = Color(0xB817263B)
private val GlassStrong = Color(0xD91A2A40)
private val GlassBorder = Color(0x66D7F2FF)

private data class LaunchableApp(val label: String, val packageName: String, val icon: Drawable)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(9, 16, 29)
        window.navigationBarColor = android.graphics.Color.rgb(9, 16, 29)
        setContent { RishLauncher() }
    }
}

@Composable
private fun RishLauncher() {
    val context = LocalContext.current
    var apps by remember { mutableStateOf(emptyList<LaunchableApp>()) }
    var showDrawer by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var now by remember { mutableStateOf(Date()) }
    var batteryPercent by remember { mutableIntStateOf(readBatteryPercent(context)) }

    LaunchedEffect(Unit) {
        apps = loadLaunchableApps(context)
        while (true) {
            now = Date()
            batteryPercent = readBatteryPercent(context)
            delay(30_000)
        }
    }
    BackHandler(showDrawer) { showDrawer = false; query = "" }

    val filteredApps = remember(apps, query) {
        apps.filter { it.label.contains(query.trim(), ignoreCase = true) }
    }
    val wideLayout = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600

    MaterialTheme(colorScheme = darkColorScheme(
        background = Color(0xFF0A1424), surface = GlassStrong,
        primary = Sky, onBackground = TextPrimary, onSurface = TextPrimary
    )) {
        BoxWithConstraints(
            Modifier.fillMaxSize().background(
                Brush.linearGradient(listOf(Color(0xFF253E60), Color(0xFF111D33), Color(0xFF080F1D)))
            ).onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when {
                    event.key == Key.Escape && showDrawer -> { showDrawer = false; query = ""; true }
                    event.key == Key.F1 || (event.key == Key.Spacebar && event.isCtrlPressed) -> {
                        showDrawer = !showDrawer
                        if (!showDrawer) query = ""
                        true
                    }
                    else -> false
                }
            }.padding(horizontal = if (wideLayout) 28.dp else 16.dp, vertical = 12.dp)
        ) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Compact macOS-inspired menu strip.
                GlassPanel(Modifier.fillMaxWidth(), radius = 18.dp, padding = 10.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(28.dp).clip(RoundedCornerShape(9.dp)).background(Sky), contentAlignment = Alignment.Center) {
                            Text("R", color = Color(0xFF0B1728), fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                        Text("Rish Deskdroid", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        if (wideLayout) {
                            Spacer(Modifier.width(8.dp))
                            Text("Desktop", color = TextMuted, fontSize = 12.sp)
                            Text("Applications", color = TextMuted, fontSize = 12.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        Text(SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(now), color = TextMuted, fontSize = 11.sp)
                        Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(now), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                if (wideLayout) {
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Desktop side rail keeps core actions grouped and discoverable.
                        GlassPanel(Modifier.width(164.dp).fillMaxHeight(), radius = 24.dp, padding = 14.dp) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("WORKSPACE", color = TextMuted, fontSize = 10.sp, letterSpacing = 1.3.sp)
                                RailAction("All apps", Icons.Default.Apps) { showDrawer = true }
                                RailAction("Settings", Icons.Default.Settings) { openSystemSettings(context) }
                                Spacer(Modifier.weight(1f))
                                Text("Simple. Focused. Yours.", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SearchField(query, { query = it; if (it.isNotBlank()) showDrawer = true })
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Your workspace", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Pick up where you left off.", color = TextMuted, fontSize = 12.sp)
                                }
                                Text("${apps.size} apps", color = Sky, fontSize = 12.sp)
                            }
                            if (apps.isEmpty()) EmptyApps(Modifier.weight(1f))
                            else LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 92.dp),
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentPadding = PaddingValues(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(apps.take(18), key = { it.packageName }) { app ->
                                    AppTile(app) { launchApp(context, app.packageName) }
                                }
                            }
                        }
                    }
                } else {
                    SearchField(query, { query = it; if (it.isNotBlank()) showDrawer = true })
                    Row(verticalAlignment = Alignment.Bottom) {
                        Column(Modifier.weight(1f)) {
                            Text("Your workspace", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                            Text("Your apps, one place.", color = TextMuted, fontSize = 12.sp)
                        }
                        Text("${apps.size} apps", color = Sky, fontSize = 12.sp)
                    }
                    if (apps.isEmpty()) EmptyApps(Modifier.weight(1f))
                    else LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 78.dp),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(apps.take(15), key = { it.packageName }) { app ->
                            AppTile(app) { launchApp(context, app.packageName) }
                        }
                    }
                }

                // Bottom glass dock + status strip. System notification shade remains native.
                GlassPanel(Modifier.fillMaxWidth(), radius = 22.dp, padding = 9.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        DockIcon("Apps", Icons.Default.Apps) { showDrawer = true }
                        DockIcon("Settings", Icons.Default.Settings) { openSystemSettings(context) }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.Wifi, contentDescription = "Wi-Fi status", tint = Sky, modifier = Modifier.size(16.dp))
                        Icon(Icons.Default.BatteryFull, contentDescription = "Battery", tint = Sky, modifier = Modifier.size(17.dp))
                        Text("$batteryPercent%", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(4.dp))
                        Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(now), color = TextMuted, fontSize = 11.sp)
                    }
                }
            }

            if (showDrawer) {
                AppDrawer(
                    apps = filteredApps, query = query,
                    onQueryChange = { query = it },
                    onDismiss = { showDrawer = false; query = "" },
                    onLaunch = { packageName ->
                        showDrawer = false; query = ""
                        launchApp(context, packageName)
                    }
                )
            }
        }
    }
}

@Composable
private fun GlassPanel(
    modifier: Modifier = Modifier,
    radius: androidx.compose.ui.unit.Dp = 20.dp,
    padding: androidx.compose.ui.unit.Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier.clip(RoundedCornerShape(radius))
            .background(Brush.verticalGradient(listOf(Color(0xD9293D58), Glass)))
            .border(1.dp, GlassBorder, RoundedCornerShape(radius))
            .padding(padding),
        content = content
    )
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(Color(0x77344862)).border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 13.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = "Search apps", tint = Sky, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = query, onValueChange = onQueryChange, singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 14.sp),
            cursorBrush = Brush.horizontalGradient(listOf(Sky, Sky)),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box {
                    if (query.isEmpty()) Text("Search apps", color = TextMuted, fontSize = 14.sp)
                    inner()
                }
            }
        )
    }
}

@Composable
private fun RailAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick)
            .background(Color(0x442D4968)).padding(horizontal = 9.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Sky, modifier = Modifier.size(18.dp))
        Text(label, color = TextPrimary, fontSize = 12.sp)
    }
}

@Composable
private fun DockIcon(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Column(
        Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(icon, contentDescription = label, tint = Sky, modifier = Modifier.size(20.dp))
        Text(label, color = TextMuted, fontSize = 9.sp)
    }
}

@Composable
private fun AppTile(app: LaunchableApp, onClick: () -> Unit) {
    val iconBitmap = remember(app.packageName) {
        runCatching { app.icon.toBitmap(width = 96, height = 96).asImageBitmap() }.getOrNull()
    }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp))
            .background(Color(0x332D4968)).border(1.dp, Color(0x2ED7F2FF), RoundedCornerShape(17.dp))
            .clickable(onClick = onClick).padding(horizontal = 4.dp, vertical = 11.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        if (iconBitmap != null) Image(bitmap = iconBitmap, contentDescription = app.label, modifier = Modifier.size(42.dp))
        else Box(Modifier.size(42.dp).clip(CircleShape).background(GlassStrong), contentAlignment = Alignment.Center) {
            Text(app.label.take(1).uppercase(), color = Sky, fontWeight = FontWeight.Bold)
        }
        Text(app.label, color = TextPrimary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun EmptyApps(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text("No launchable apps found.", color = TextMuted, fontSize = 14.sp)
    }
}

@Composable
private fun AppDrawer(
    apps: List<LaunchableApp>, query: String,
    onQueryChange: (String) -> Unit, onDismiss: () -> Unit, onLaunch: (String) -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color(0xE608101D)).padding(12.dp)) {
        Column(
            Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))
                .background(Color(0xF21A2A40)).border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Applications", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text("${apps.size} matching apps", color = TextMuted, fontSize = 11.sp)
                }
                Text("Close  ×", color = Sky, fontSize = 13.sp,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onDismiss).padding(8.dp))
            }
            Spacer(Modifier.height(12.dp))
            SearchField(query, onQueryChange)
            Spacer(Modifier.height(8.dp))
            if (apps.isEmpty()) EmptyApps(Modifier.weight(1f))
            else LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 78.dp),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                items(apps, key = { it.packageName }) { app -> AppTile(app) { onLaunch(app.packageName) } }
            }
        }
    }
}

private fun loadLaunchableApps(context: Context): List<LaunchableApp> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return pm.queryIntentActivities(intent, 0).mapNotNull { info ->
        val activity = info.activityInfo ?: return@mapNotNull null
        if (activity.packageName == context.packageName) return@mapNotNull null
        LaunchableApp(info.loadLabel(pm).toString(), activity.packageName, info.loadIcon(pm))
    }.distinctBy { it.packageName }.sortedBy { it.label.lowercase(Locale.getDefault()) }
}

private fun launchApp(context: Context, packageName: String) {
    runCatching {
        context.packageManager.getLaunchIntentForPackage(packageName)?.let { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}

private fun openSystemSettings(context: Context) {
    runCatching { context.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS)) }
}

private fun readBatteryPercent(context: Context): Int {
    val manager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val level = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    return level.coerceIn(0, 100)
}

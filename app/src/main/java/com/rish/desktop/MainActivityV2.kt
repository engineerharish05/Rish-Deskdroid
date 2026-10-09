package com.rish.desktop

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Night = Color(0xFF0A1020)
private val Blue = Color(0xFF9EDBFF)
private val WhiteText = Color(0xFFF3F7FF)
private val MutedText = Color(0xFFB2C0D6)
private val Panel = Color(0xB91B2940)
private val PanelEdge = Color(0x42E4F4FF)

private data class DesktopApp(val name: String, val packageName: String, val icon: Drawable)

class MainActivityV2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(10, 16, 32)
        window.navigationBarColor = android.graphics.Color.rgb(10, 16, 32)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Blue, background = Night, surface = Panel,
                onBackground = WhiteText, onSurface = WhiteText
            )) { DesktopV2() }
        }
    }
}

@Composable
private fun DesktopV2() {
    val context = LocalContext.current
    val wide = LocalConfiguration.current.screenWidthDp >= 600
    var apps by remember { mutableStateOf(emptyList<DesktopApp>()) }
    var search by remember { mutableStateOf("") }
    var drawerOpen by remember { mutableStateOf(false) }
    var clock by remember { mutableStateOf(Date()) }
    var battery by remember { mutableIntStateOf(batteryLevel(context)) }

    LaunchedEffect(Unit) {
        apps = loadApps(context)
        while (true) {
            clock = Date()
            battery = batteryLevel(context)
            delay(30_000)
        }
    }
    BackHandler(drawerOpen) { drawerOpen = false; search = "" }
    val visibleApps = remember(apps, search) {
        apps.filter { it.name.contains(search.trim(), ignoreCase = true) }
    }

    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(
                listOf(Color(0xFF29466A), Color(0xFF14233B), Night),
                start = androidx.compose.ui.geometry.Offset.Zero,
                end = androidx.compose.ui.geometry.Offset(900f, 1500f)
            )
        ).padding(if (wide) 24.dp else 14.dp)
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Glass(Modifier.fillMaxWidth(), 18.dp, 10.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(30.dp).clip(RoundedCornerShape(10.dp)).background(Blue), contentAlignment = Alignment.Center) {
                        Text("R", color = Night, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                    Text("Rish Deskdroid", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    if (wide) {
                        Text("Workspace", color = MutedText, fontSize = 12.sp)
                        Text("Applications", color = MutedText, fontSize = 12.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(clock), color = MutedText, fontSize = 11.sp)
                    Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(clock), color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            if (wide) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Glass(Modifier.width(170.dp).fillMaxHeight(), 24.dp, 14.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("WORKSPACE", color = MutedText, fontSize = 10.sp, letterSpacing = 1.5.sp)
                            SideAction("All applications", Icons.Default.Apps) { drawerOpen = true }
                            SideAction("System settings", Icons.Default.Settings) { openSettings(context) }
                            Spacer(Modifier.weight(1f))
                            Text("A calmer home for your apps.", color = MutedText, fontSize = 11.sp)
                        }
                    }
                    Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SearchBox(search, { search = it; if (it.isNotBlank()) drawerOpen = true })
                        Header(apps.size)
                        if (apps.isEmpty()) EmptyState(Modifier.weight(1f))
                        else AppGrid(apps.take(20), Modifier.weight(1f), 94.dp) { launch(context, it) }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Good to see you.", color = WhiteText, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
                    Text(SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(clock), color = MutedText, fontSize = 13.sp)
                }
                SearchBox(search, { search = it; if (it.isNotBlank()) drawerOpen = true })
                Header(apps.size)
                if (apps.isEmpty()) EmptyState(Modifier.weight(1f))
                else AppGrid(apps.take(15), Modifier.weight(1f), 76.dp) { launch(context, it) }
            }

            Glass(Modifier.fillMaxWidth(), 22.dp, 8.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DockItem("Apps", Icons.Default.Apps) { drawerOpen = true }
                    DockItem("Settings", Icons.Default.Settings) { openSettings(context) }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.Wifi, contentDescription = "Wi-Fi indicator", tint = Blue, modifier = Modifier.size(16.dp))
                    Icon(Icons.Default.BatteryFull, contentDescription = "Battery", tint = Blue, modifier = Modifier.size(17.dp))
                    Text("$battery%", color = WhiteText, fontSize = 11.sp)
                }
            }
        }

        if (drawerOpen) {
            Dialog(onDismissRequest = { drawerOpen = false; search = "" }) {
                Surface(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.82f),
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xF0152238),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PanelEdge)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Applications", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                                Text("${visibleApps.size} apps available", color = MutedText, fontSize = 12.sp)
                            }
                            Text("Done", color = Blue, modifier = Modifier.clickable { drawerOpen = false; search = "" }.padding(8.dp))
                        }
                        SearchBox(search, { search = it })
                        if (visibleApps.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No apps match your search.", color = MutedText)
                            }
                        } else {
                            AppGrid(visibleApps, Modifier.weight(1f), 78.dp) {
                                drawerOpen = false
                                search = ""
                                launch(context, it)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Glass(modifier: Modifier, radius: androidx.compose.ui.unit.Dp, padding: androidx.compose.ui.unit.Dp, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.clip(RoundedCornerShape(radius)).background(Panel)
            .border(1.dp, PanelEdge, RoundedCornerShape(radius))
            .then(modifier).padding(padding),
        content = content
    )
}

@Composable
private fun SearchBox(value: String, onChange: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(Color(0x80101B2D)).border(1.dp, PanelEdge, RoundedCornerShape(16.dp))
            .padding(horizontal = 13.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Icon(Icons.Default.Search, contentDescription = "Search", tint = MutedText, modifier = Modifier.size(19.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text("Search apps", color = MutedText, fontSize = 14.sp)
            BasicTextField(
                value = value, onValueChange = onChange, singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = WhiteText, fontSize = 14.sp),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun Header(count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Your workspace", color = WhiteText, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
            Text("Your everyday apps, in one place.", color = MutedText, fontSize = 12.sp)
        }
        Text("$count apps", color = Blue, fontSize = 12.sp)
    }
}

@Composable
private fun AppGrid(apps: List<DesktopApp>, modifier: Modifier, cell: androidx.compose.ui.unit.Dp, onOpen: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = cell),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(apps, key = { it.packageName }) { app ->
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                    .clickable { onOpen(app.packageName) }.padding(vertical = 10.dp, horizontal = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Image(
                    bitmap = remember(app.packageName) { app.icon.toBitmap(96, 96).asImageBitmap() },
                    contentDescription = app.name,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                )
                Text(app.name, color = WhiteText, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Apps, contentDescription = null, tint = Blue, modifier = Modifier.size(42.dp))
            Text("No launchable apps found", color = WhiteText, fontWeight = FontWeight.Medium)
            Text("Apps installed on this device will appear here.", color = MutedText, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SideAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = Blue, modifier = Modifier.size(18.dp))
        Text(label, color = WhiteText, fontSize = 12.sp)
    }
}

@Composable
private fun DockItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(Modifier.clip(RoundedCornerShape(13.dp)).clickable(onClick = onClick).padding(horizontal = 9.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, tint = Blue, modifier = Modifier.size(18.dp))
        Text(label, color = WhiteText, fontSize = 11.sp)
    }
}

private fun loadApps(context: Context): List<DesktopApp> {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return context.packageManager.queryIntentActivities(intent, 0)
        .mapNotNull { info ->
            val packageName = info.activityInfo?.packageName ?: return@mapNotNull null
            DesktopApp(info.loadLabel(context.packageManager).toString(), packageName, info.loadIcon(context.packageManager))
        }
        .distinctBy { it.packageName }
        .sortedBy { it.name.lowercase(Locale.getDefault()) }
}

private fun launch(context: Context, packageName: String) {
    try { context.packageManager.getLaunchIntentForPackage(packageName)?.let { context.startActivity(it) } }
    catch (_: Exception) { }
}

private fun openSettings(context: Context) {
    try { context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    catch (_: Exception) { }
}

private fun batteryLevel(context: Context): Int {
    val manager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    return manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).coerceIn(0, 100)
}

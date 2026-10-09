package com.rish.deskdroid

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Navy = Color(0xFF12163A)
private val Glass = Color(0x35FFFFFF)
private val Line = Color(0x55FFFFFF)
private data class LaunchApp(val label: String, val packageName: String, val icon: Drawable)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        window.decorView.systemUiVisibility =
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF9EDBFF))) {
                DeskDroidApp()
            }
        }
    }
}

@Composable
private fun DeskDroidApp() {
    val context = LocalContext.current
    val pm = context.packageManager
    var page by remember { mutableStateOf("home") }
    var query by remember { mutableStateOf("") }
    var clock by remember { mutableStateOf(timeNow()) }
    var quickPanel by remember { mutableStateOf(false) }
    var dockEditor by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf("") }
    var pinned by remember {
        mutableStateOf(
            context.getSharedPreferences("deskdroid", Context.MODE_PRIVATE)
                .getStringSet("pinned", setOf("Chrome", "YouTube", "Settings", "Camera"))
                ?.toSet() ?: setOf("Chrome", "YouTube", "Settings", "Camera")
        )
    }

    LaunchedEffect(Unit) {
        while (true) { clock = timeNow(); delay(15_000) }
    }

    val apps = remember {
        pm.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
            PackageManager.MATCH_ALL
        ).mapNotNull { info ->
            val label = info.loadLabel(pm)?.toString()?.trim().orEmpty()
            val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
            if (label.isBlank() || pkg == context.packageName) null
            else LaunchApp(label, pkg, info.loadIcon(pm))
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase(Locale.getDefault()) }
    }
    val filtered = apps.filter { it.label.contains(query, ignoreCase = true) }
    val dockApps = pinned.mapNotNull { key ->
        apps.firstOrNull { it.label.equals(key, true) || it.packageName.substringAfterLast('.').equals(key, true) }
    }.take(5)

    BackHandler(page != "home" || quickPanel || dockEditor) {
        when {
            dockEditor -> dockEditor = false
            quickPanel -> quickPanel = false
            else -> { page = "home"; query = "" }
        }
    }

    BoxWithConstraints(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(listOf(Navy, Color(0xFF173B70), Color(0xFF3857B8), Color(0xFF8A4DFF), Color(0xFFFF6AA0)))
        )
    ) {
        Box(Modifier.offset(x = maxWidth * .58f, y = (-90).dp).size(300.dp).background(Color(0x226FD6FF), CircleShape))
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                    .background(Color(0x35101430)).clickable { quickPanel = !quickPanel }
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Rish Deskdroid", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("•", color = Color.White.copy(.6f))
                    Text(clock, color = Color.White, fontSize = 13.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Icon(Icons.Default.Wifi, "Quick settings", tint = Color.White, modifier = Modifier.size(18.dp))
                    Icon(Icons.Default.Bluetooth, "Bluetooth", tint = Color.White, modifier = Modifier.size(18.dp))
                    Icon(Icons.Default.BatteryFull, "Battery", tint = Color.White, modifier = Modifier.size(19.dp))
                }
            }

            AnimatedVisibility(quickPanel) {
                Card(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xE91A2044)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Quick settings", color = Color.White, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            QuickTile("Wi-Fi", Icons.Default.Wifi) { openSettings(context, Settings.ACTION_WIFI_SETTINGS) }
                            QuickTile("Bluetooth", Icons.Default.Bluetooth) { openSettings(context, Settings.ACTION_BLUETOOTH_SETTINGS) }
                            QuickTile("Display", Icons.Default.BrightnessMedium) { openSettings(context, Settings.ACTION_DISPLAY_SETTINGS) }
                            QuickTile("System", Icons.Default.Settings) { openSettings(context, Settings.ACTION_SETTINGS) }
                        }
                    }
                }
            }

            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (page == "home") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(clock, color = Color.White, fontSize = 46.sp, fontWeight = FontWeight.Light)
                        Text(SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()), color = Color.White.copy(.82f), fontSize = 14.sp)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            HomeShortcut("All apps", Icons.Default.Apps) { page = "drawer"; query = "" }
                            HomeShortcut("Search", Icons.Default.Search) { page = "drawer"; query = "" }
                            HomeShortcut("Settings", Icons.Default.Settings) { openSettings(context, Settings.ACTION_SETTINGS) }
                        }
                    }
                } else {
                    Column(Modifier.fillMaxSize().padding(top = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { page = "home"; query = "" }) {
                                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                            }
                            Text("App library", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.weight(1f))
                            Text(filtered.size.toString() + " apps", color = Color.White.copy(.7f), fontSize = 12.sp)
                        }
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Glass)
                                .padding(horizontal = 12.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, null, tint = Color.White.copy(.8f))
                            Spacer(Modifier.width(8.dp))
                            BasicTextField(
                                value = query, onValueChange = { query = it }, singleLine = true,
                                modifier = Modifier.weight(1f),
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp),
                                decorationBox = { inner ->
                                    if (query.isEmpty()) Text("Search installed apps", color = Color.White.copy(.6f))
                                    inner()
                                }
                            )
                            if (query.isNotEmpty()) IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Close, "Clear search", tint = Color.White)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        if (filtered.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No apps match this search", color = Color.White.copy(.8f))
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 78.dp),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(filtered, key = { it.packageName }) { app ->
                                    AppTile(app, onOpen = { launchApp(context, app) }, onLongPress = {
                                        pinned = if (pinned.contains(app.label)) pinned - app.label else pinned + app.label
                                        savePinned(context, pinned)
                                        notice = app.label + if (pinned.contains(app.label)) " pinned to dock" else " removed from dock"
                                    })
                                }
                            }
                        }
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
                    .background(Brush.verticalGradient(listOf(Color(0x55FFFFFF), Color(0x25FFFFFF))))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                dockApps.forEach { app ->
                    AppTile(app, compact = true, onOpen = { launchApp(context, app) }, onLongPress = {
                        pinned = pinned - app.label
                        savePinned(context, pinned)
                        notice = app.label + " removed from dock"
                    })
                }
                DockAction(Icons.Default.Apps, "Apps") { page = if (page == "drawer") "home" else "drawer"; query = "" }
                DockAction(Icons.Default.Tune, "Dock") { dockEditor = true }
            }
        }

        if (dockEditor) {
            AlertDialog(
                onDismissRequest = { dockEditor = false },
                title = { Text("Customize dock") },
                text = {
                    Column {
                        Text("Choose which installed apps appear in the dock.")
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(Modifier.heightIn(max = 320.dp)) {
                            items(apps) { app ->
                                Row(
                                    Modifier.fillMaxWidth().clickable {
                                        pinned = if (pinned.contains(app.label)) pinned - app.label else pinned + app.label
                                        savePinned(context, pinned)
                                    }.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(app.label, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Checkbox(pinned.contains(app.label), onCheckedChange = {
                                        pinned = if (it) pinned + app.label else pinned - app.label
                                        savePinned(context, pinned)
                                    })
                                }
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { dockEditor = false }) { Text("Done") } }
            )
        }
        if (notice.isNotBlank()) {
            LaunchedEffect(notice) { delay(1800); notice = "" }
            Snackbar(
                Modifier.align(Alignment.BottomCenter).padding(bottom = 90.dp),
                containerColor = Color(0xEE171B35)
            ) { Text(notice, color = Color.White) }
        }
    }
}

@Composable
private fun QuickTile(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Column(
        Modifier.width(70.dp).clip(RoundedCornerShape(14.dp)).background(Glass).clickable(onClick = onClick).padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = Color.White)
        Spacer(Modifier.height(4.dp))
        Text(title, color = Color.White, fontSize = 10.sp)
    }
}

@Composable
private fun HomeShortcut(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Column(
        Modifier.clip(RoundedCornerShape(16.dp)).background(Glass).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(5.dp))
        Text(title, color = Color.White, fontSize = 11.sp)
    }
}

@Composable
private fun DockAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        Modifier.padding(horizontal = 4.dp).clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(Color(0x667A96FF)), contentAlignment = Alignment.Center) {
            Icon(icon, label, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Text(label, color = Color.White, fontSize = 9.sp, maxLines = 1)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppTile(app: LaunchApp, compact: Boolean = false, onOpen: () -> Unit, onLongPress: () -> Unit) {
    val iconSize = if (compact) 42.dp else 50.dp
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .combinedClickable(onClick = onOpen, onLongClick = onLongPress)
            .padding(horizontal = 2.dp, vertical = if (compact) 1.dp else 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val bitmap = remember(app.packageName) {
            val bmp = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            app.icon.setBounds(0, 0, 96, 96)
            app.icon.draw(canvas)
            bmp.asImageBitmap()
        }
        androidx.compose.foundation.Image(
            bitmap = bitmap, contentDescription = app.label,
            modifier = Modifier.size(iconSize).clip(RoundedCornerShape(14.dp)).background(Color(0x33FFFFFF)).padding(2.dp)
        )
        if (!compact) Text(app.label, color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private fun savePinned(context: Context, pinned: Set<String>) {
    context.getSharedPreferences("deskdroid", Context.MODE_PRIVATE).edit().putStringSet("pinned", pinned).apply()
}

private fun launchApp(context: Context, app: LaunchApp) {
    try {
        context.packageManager.getLaunchIntentForPackage(app.packageName)?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    } catch (_: Exception) { }
}

private fun openSettings(context: Context, action: String) {
    try { context.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    catch (_: Exception) { context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

private fun timeNow(): String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

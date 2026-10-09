package com.rish.desktop

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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

private val BackgroundTop = Color(0xFF172238)
private val BackgroundBottom = Color(0xFF080D17)
private val Panel = Color(0xFF1B2940)
private val PanelRaised = Color(0xFF24344D)
private val Accent = Color(0xFF91DFFF)
private val MainText = Color(0xFFF2F6FC)
private val MutedText = Color(0xFFACB9CD)

private data class LaunchableApp(
    val label: String,
    val packageName: String,
    val icon: Drawable
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(8, 13, 23)
        window.navigationBarColor = android.graphics.Color.rgb(8, 13, 23)
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

    LaunchedEffect(Unit) {
        apps = loadLaunchableApps(context)
        while (true) {
            now = Date()
            delay(30_000)
        }
    }

    BackHandler(showDrawer) {
        showDrawer = false
        query = ""
    }

    val filteredApps = remember(apps, query) {
        apps.filter { it.label.contains(query.trim(), ignoreCase = true) }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = BackgroundBottom,
            surface = Panel,
            primary = Accent,
            onBackground = MainText,
            onSurface = MainText
        )
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom)))
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when {
                        event.key == Key.Escape && showDrawer -> {
                            showDrawer = false
                            query = ""
                            true
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
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(Accent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("R", color = BackgroundBottom, fontWeight = FontWeight.Black, fontSize = 24.sp)
                        }
                        Column {
                            Text(
                                "RISH DESKDROID",
                                color = MainText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = 1.5.sp
                            )
                            Text("Your apps, one place", color = MutedText, fontSize = 11.sp)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(now),
                            color = MainText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        )
                        Text(
                            SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(now),
                            color = MutedText,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(PanelRaised)
                        .padding(horizontal = 13.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search apps", tint = Accent)
                    Spacer(Modifier.width(10.dp))
                    BasicTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            if (it.isNotBlank()) showDrawer = true
                        },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = MainText, fontSize = 14.sp),
                        cursorBrush = Brush.horizontalGradient(listOf(Accent, Accent)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            Box {
                                if (query.isEmpty()) Text("Search apps…", color = MutedText, fontSize = 14.sp)
                                inner()
                            }
                        }
                    )
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("QUICK LAUNCH", color = MutedText, fontSize = 11.sp, letterSpacing = 1.7.sp)
                    Text(
                        "All apps  →",
                        color = Accent,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showDrawer = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                if (apps.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No launchable apps found.", color = MutedText, fontSize = 14.sp)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 82.dp),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(apps.take(12), key = { it.packageName }) { app ->
                            AppTile(app) { launchApp(context, app.packageName) }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(15.dp))
                        .background(Panel)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DockAction("Apps", Icons.Default.Apps) { showDrawer = true }
                    DockAction("Settings", Icons.Default.Settings) { openSystemSettings(context) }
                    Spacer(Modifier.weight(1f))
                    Text("Ctrl + Space · F1", color = MutedText, fontSize = 10.sp)
                }
            }

            if (showDrawer) {
                AppDrawer(
                    apps = filteredApps,
                    query = query,
                    onQueryChange = { query = it },
                    onDismiss = {
                        showDrawer = false
                        query = ""
                    },
                    onLaunch = { packageName ->
                        showDrawer = false
                        query = ""
                        launchApp(context, packageName)
                    }
                )
            }
        }
    }
}

private fun loadLaunchableApps(context: Context): List<LaunchableApp> {
    val packageManager = context.packageManager
    val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

    return packageManager.queryIntentActivities(launcherIntent, 0)
        .mapNotNull { info ->
            val activity = info.activityInfo ?: return@mapNotNull null
            if (activity.packageName == context.packageName) return@mapNotNull null
            LaunchableApp(
                label = info.loadLabel(packageManager).toString(),
                packageName = activity.packageName,
                icon = info.loadIcon(packageManager)
            )
        }
        .distinctBy { it.packageName }
        .sortedBy { it.label.lowercase(Locale.getDefault()) }
}

@Composable
private fun AppTile(app: LaunchableApp, onClick: () -> Unit) {
    val iconBitmap = remember(app.packageName) {
        app.icon.toBitmap(width = 96, height = 96).asImageBitmap()
    }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Image(bitmap = iconBitmap, contentDescription = app.label, modifier = Modifier.size(42.dp))
        Text(
            app.label,
            color = MainText,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DockAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Accent, modifier = Modifier.size(17.dp))
        Text(label, color = MainText, fontSize = 11.sp)
    }
}

@Composable
private fun AppDrawer(
    apps: List<LaunchableApp>,
    query: String,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onLaunch: (String) -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xF2080D17))
            .padding(12.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(BackgroundTop)
                .padding(16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("ALL APPS", color = MainText, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    Text(apps.size.toString() + " matching apps", color = MutedText, fontSize = 11.sp)
                }
                Text(
                    "Close  ✕",
                    color = Accent,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onDismiss)
                        .padding(8.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PanelRaised)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Accent)
                Spacer(Modifier.width(9.dp))
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = MainText, fontSize = 14.sp),
                    cursorBrush = Brush.horizontalGradient(listOf(Accent, Accent)),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        Box {
                            if (query.isEmpty()) Text("Find an app…", color = MutedText, fontSize = 14.sp)
                            inner()
                        }
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            if (apps.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No matching apps. Try another search.", color = MutedText, fontSize = 13.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 82.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    items(apps, key = { it.packageName }) { app ->
                        AppTile(app) { onLaunch(app.packageName) }
                    }
                }
            }
        }
    }
}

private fun launchApp(context: Context, packageName: String) {
    runCatching {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        }
    }
}

private fun openSystemSettings(context: Context) {
    runCatching {
        context.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
    }
}

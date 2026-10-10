package com.rishdeskdroid.app.ui

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rishdeskdroid.app.data.AppInfo
import com.rishdeskdroid.app.data.LauncherPrefs
import com.rishdeskdroid.app.data.WidgetController
import com.rishdeskdroid.app.data.addToDock
import com.rishdeskdroid.app.data.dockApps
import com.rishdeskdroid.app.data.launchApp
import com.rishdeskdroid.app.data.openLauncherChooser
import com.rishdeskdroid.app.data.openMobileSettings
import com.rishdeskdroid.app.data.removeFromDock

/**
 * The home screen: widgets in the middle, the glass dock along the bottom.
 * Swipe up anywhere (or tap the grid tile) to open the app drawer.
 */
@Composable
fun HomeScreen(
    prefs: LauncherPrefs,
    apps: List<AppInfo>,
    widgets: WidgetController,
    otgConnected: Boolean,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    otgConnected: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showGlass by remember { mutableStateOf(false) }
    var showAddPicker by remember { mutableStateOf(false) }

    Box(
        modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var total = 0f
                detectVerticalDragGestures(
                    onDragStart = { total = 0f },
                    onDragEnd = { if (total < -90f) onOpenDrawer() },
                    onDragCancel = { total = 0f },
                    onVerticalDrag = { _, delta -> total += delta },
                )
            },
    ) {
        // Widgets (only if enabled and at least one has been added)
        if (prefs.widgetsEnabled && prefs.widgetIds.isNotEmpty()) {
            Row(
                Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                prefs.widgetIds.forEach { id ->
                    key(id) { WidgetView(widgets, id) }
                }
            }
        }

        // Dock
        Dock(
            prefs = prefs,
            apps = apps,
            onGlass = { showGlass = true },
            onAdd = { showAddPicker = true },
            onOpenDrawer = onOpenDrawer,
            onOpenSettings = onOpenSettings,
            otgConnected = otgConnected,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 8.dp, vertical = 12.dp),
        )

        // Glass settings popover (the handle at the left end of the dock)
        if (showGlass) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { showGlass = false },
            )
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 84.dp)
                    .width(280.dp)
                    .glass(RoundedCornerShape(20.dp), prefs.glassOpacity, dark = true)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Glass opacity",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "${(prefs.glassOpacity * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                GlassSlider(
                    value = prefs.glassOpacity,
                    onValueChange = { prefs.updateGlassOpacity(it) },
                )
            }
        }
    }

    if (showAddPicker) {
        val pinned = dockApps(prefs, apps).map { it.packageName }
        AppPickerDialog(
            title = "Add to dock",
            apps = apps.filter { it.packageName !in pinned },
            onPick = { app ->
                showAddPicker = false
                if (!addToDock(prefs, apps, app)) {
                    Toast.makeText(context, "The dock is full", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showAddPicker = false },
        )
    }
}

@Composable
fun GlassSlider(value: Float, onValueChange: (Float) -> Unit, modifier: Modifier = Modifier) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = 0f..1f,
        modifier = modifier.semantics { contentDescription = "Glass opacity" },
        colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = Color(0xFF7AA7FF),
            inactiveTrackColor = Color.White.copy(alpha = 0.3f),
        ),
    )
}

@Composable
private fun Dock(
    prefs: LauncherPrefs,
    apps: List<AppInfo>,
    onGlass: () -> Unit,
    onAdd: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val dock = dockApps(prefs, apps)
    val tile = 48.dp

    Row(
        modifier
            .glass(RoundedCornerShape(26.dp), prefs.glassOpacity)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Glass settings handle
        val handleSource = remember { MutableInteractionSource() }
        Box(
            Modifier
                .size(width = 24.dp, height = tile)
                .pressScale(handleSource, 1.2f)
                .clickable(interactionSource = handleSource, indication = null) { onGlass() }
                .semantics { contentDescription = "Glass settings" },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(width = 4.dp, height = 26.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.7f)),
            )
        }

        // Mobile settings
        IconTile(tile, Tiles.gray, "Mobile settings", onClick = { openMobileSettings(context) }) {
            SvgIcon(Ico.gear, 26.dp)
        }

        // Pinned apps: Chrome, YouTube, Google, GPT by default
        dock.forEach { app ->
            key(app.key) {
                DockApp(app, tile, onRemove = { removeFromDock(prefs, apps, app) })
            }
        }

        // OTG shortcut: opens launcher settings so the OTG controls are visible
        if (prefs.otgEnabled && otgConnected) {
            IconTile(tile, Tiles.green, "OTG settings", onClick = onOpenSettings) {
                SvgIcon(Ico.usb, 26.dp)
            }
        }

        // + add an app
        IconTile(
            size = tile,
            brush = Tiles.glass,
            label = "Add an app",
            onClick = onAdd,
            decoration = Modifier.dashedBorder(Color.White.copy(alpha = 0.75f), tile * 0.24f),
        ) {
            SvgIcon(Ico.plus, 24.dp, strokeWidth = 2.2f)
        }

        // App drawer
        IconTile(tile, Tiles.blue, "App drawer", onClick = onOpenDrawer) {
            SvgIcon(Ico.grid, 26.dp)
        }

        Box(
            Modifier
                .size(width = 1.dp, height = 38.dp)
                .background(Color.White.copy(alpha = 0.45f)),
        )

        // App settings
        IconTile(tile, Tiles.graphite, "App settings", onClick = onOpenSettings) {
            SvgIcon(Ico.sliders, 26.dp)
        }

        // Exit / switch launcher
        IconTile(tile, Tiles.red, "Exit launcher", onClick = { openLauncherChooser(context) }) {
            SvgIcon(Ico.exit, 26.dp)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DockApp(
    app: AppInfo,
    tile: androidx.compose.ui.unit.Dp,
    onRemove: () -> Unit,
) {
    val context = LocalContext.current
    var menu by remember { mutableStateOf(false) }
    val source = remember { MutableInteractionSource() }

    Box {
        Box(
            Modifier
                .size(tile)
                .pressScale(source)
                .combinedClickable(
                    interactionSource = source,
                    indication = null,
                    onClick = { launchApp(context, app) },
                    onLongClick = { menu = true },
                )
                .semantics { contentDescription = app.label },
        ) {
            AppIconImage(app, tile)
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(
                text = { Text("Remove from dock") },
                onClick = {
                    menu = false
                    onRemove()
                },
            )
        }
    }
}

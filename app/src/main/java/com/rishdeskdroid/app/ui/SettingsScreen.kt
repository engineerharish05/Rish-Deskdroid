package com.rishdeskdroid.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rishdeskdroid.app.data.LauncherPrefs
import com.rishdeskdroid.app.data.WallpaperStore
import com.rishdeskdroid.app.data.WidgetController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * App settings: Widgets, OTG Shortcut, Keyboard shortcuts, Glass Opacity and Wallpaper change.
 * Leave with the back button.
 */
@Composable
fun SettingsScreen(
    prefs: LauncherPrefs,
    widgets: WidgetController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showWidgetPicker by remember { mutableStateOf(false) }
    var showWallpaper by remember { mutableStateOf(false) }

    // Wallpaper: the photo picked but not applied yet
    var pending by remember { mutableStateOf<ImageBitmap?>(null) }
    var pendingRaw by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    val current by produceState<ImageBitmap?>(initialValue = null, key1 = prefs.wallpaperVersion) {
        value = withContext(Dispatchers.IO) { WallpaperStore.load(context) }
    }
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && showWallpaper) {
            scope.launch {
                val bitmap = withContext(Dispatchers.IO) { WallpaperStore.decode(context, uri) }
                if (bitmap != null) {
                    pendingRaw = bitmap
                    pending = bitmap.asImageBitmap()
                }
            }
        }
    }

    Column(
        modifier
            .fillMaxSize()
            // Swallow taps so nothing underneath reacts
            .pointerInput(Unit) { detectTapGestures { } }
            .padding(horizontal = 28.dp),
    ) {
        Text(
            "Settings",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            style = LabelStyle,
            modifier = Modifier.padding(top = 6.dp, bottom = 8.dp),
        )

        Column(
            Modifier
                .weight(1f, fill = false)
                .fillMaxWidth()
                .glass(RoundedCornerShape(22.dp), prefs.glassOpacity)
                .verticalScroll(rememberScrollState()),
        ) {
            // Widgets
            SettingRow(Ico.widgets, Tiles.blue, "Widgets") {
                Row(
                    Modifier
                        .weight(1f, fill = false)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    prefs.widgetIds.forEach { id ->
                        val label = remember(id) {
                            widgets.manager.getAppWidgetInfo(id)?.loadLabel(context.packageManager) ?: "Widget"
                        }
                        WidgetChip(label) { widgets.remove(id) }
                    }
                }
                SquareButton("Select widget", onClick = { showWidgetPicker = true }) {
                    SvgIcon(Ico.plus, 22.dp, strokeWidth = 2.2f)
                }
                GlassSwitch(prefs.widgetsEnabled, { prefs.updateWidgetsEnabled(it) }, "Widgets")
            }
            RowDivider()

            // OTG Shortcut
            SettingRow(
                Ico.usb, Tiles.green, "OTG Shortcut",
                subtitle = "Adds a dock shortcut to the Android USB / storage screen while a USB device is plugged in",
            ) {
                GlassSwitch(prefs.otgEnabled, { prefs.updateOtgEnabled(it) }, "OTG Shortcut")
            }
            RowDivider()

            // Keyboard shortcuts
            SettingRow(
                Ico.keyboard, Tiles.graphite, "Keyboard shortcuts",
                subtitle = "Ctrl+D drawer · Ctrl+, settings · Ctrl+H home · Ctrl+1-9 dock apps · Esc back",
            ) {
                GlassSwitch(prefs.keyboardEnabled, { prefs.updateKeyboardEnabled(it) }, "Keyboard shortcuts")
            }
            RowDivider()

            // Glass Opacity
            SettingRow(Ico.drop, Tiles.sky, "Glass Opacity") {
                GlassSlider(
                    value = prefs.glassOpacity,
                    onValueChange = { prefs.updateGlassOpacity(it) },
                    modifier = Modifier.widthIn(max = 320.dp).weight(1f, fill = false),
                )
                Text(
                    "${(prefs.glassOpacity * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(44.dp),
                )
            }
            RowDivider()

            // Wallpaper change: tap the preview (or Change) to open the wallpaper controls
            SettingRow(Ico.image, Tiles.pink, "Wallpaper change") {
                val thumbShape = RoundedCornerShape(8.dp)
                val shown = current
                Box(
                    Modifier
                        .size(width = 84.dp, height = 48.dp)
                        .clip(thumbShape)
                        .border(1.dp, Color.White.copy(alpha = 0.5f), thumbShape)
                        .clickable(
                            onClickLabel = "Open wallpaper controls",
                            role = Role.Button,
                        ) { showWallpaper = true }
                        .semantics { contentDescription = "Wallpaper preview. Tap to change wallpaper" },
                ) {
                    if (shown != null) {
                        Image(
                            bitmap = shown,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        DefaultWallpaper(Modifier.fillMaxSize())
                    }
                }
                TextButton("Change") { showWallpaper = true }
            }
        }
        Spacer(Modifier.height(10.dp))
    }

    if (showWallpaper) {
        WallpaperDialog(
            preview = pending ?: current,
            hasCustomWallpaper = current != null,
            hasPending = pendingRaw != null,
            onSelectPhoto = { pickPhoto.launch("image/*") },
            onUseDefault = {
                pending = null
                pendingRaw = null
                WallpaperStore.clear(context)
                prefs.bumpWallpaper()
                showWallpaper = false
            },
            onApply = {
                val raw = pendingRaw
                if (raw != null) {
                    scope.launch {
                        withContext(Dispatchers.IO) { WallpaperStore.save(context, raw) }
                        pending = null
                        pendingRaw = null
                        prefs.bumpWallpaper()
                        showWallpaper = false
                    }
                }
            },
            onCancel = {
                // Closing without Apply keeps the current wallpaper untouched
                pending = null
                pendingRaw = null
                showWallpaper = false
            },
        )
    }

    if (showWidgetPicker) {
        WidgetPickerDialog(
            providers = remember { widgets.providers() },
            onPick = { info ->
                showWidgetPicker = false
                widgets.beginAdd(info)
            },
            onDismiss = { showWidgetPicker = false },
        )
    }
}

@Composable
private fun SettingRow(
    icon: IconDef,
    brush: Brush,
    title: String,
    subtitle: String? = null,
    trailing: @Composable RowScope.() -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(brush),
            contentAlignment = Alignment.Center,
        ) {
            SvgIcon(icon, 22.dp)
        }
        Column(
            Modifier
                .padding(start = 14.dp, end = 12.dp)
                .widthIn(min = 150.dp, max = 260.dp),
        ) {
            Text(
                title,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                style = LabelStyle,
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                )
            }
        }
        Row(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
            content = trailing,
        )
    }
}

@Composable
private fun RowDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.14f)),
    )
}

@Composable
private fun WidgetChip(label: String, onRemove: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier
            .heightIn(min = 32.dp)
            .clip(shape)
            .background(Color.White.copy(alpha = 0.18f))
            .clickable(onClickLabel = "Remove $label", onClick = onRemove)
            .padding(start = 12.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(label, color = Color.White, fontSize = 13.sp, maxLines = 1)
        SvgIcon(Ico.close, 14.dp, strokeWidth = 2.4f)
    }
}

@Composable
private fun SquareButton(label: String, onClick: () -> Unit, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        Modifier
            .size(44.dp)
            .clip(shape)
            .background(Color.White.copy(alpha = 0.2f))
            .border(1.dp, Color.White.copy(alpha = 0.4f), shape)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = label
                role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun TextButton(
    text: String,
    primary: Boolean = false,
    enabled: Boolean = true,
    fullWidth: Boolean = false,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    val fill = if (primary) Color(0xFF3B82F6) else Color.White.copy(alpha = 0.2f)
    Box(
        Modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = 44.dp)
            .clip(shape)
            .background(fill.copy(alpha = if (enabled) fill.alpha else fill.alpha * 0.4f))
            .border(1.dp, Color.White.copy(alpha = if (primary) 0f else 0.4f), shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = Color.White.copy(alpha = if (enabled) 1f else 0.5f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}


/** Wallpaper controls: pick a photo, preview it, then Apply, or go back to the default / cancel. */
@Composable
private fun WallpaperDialog(
    preview: ImageBitmap?,
    hasCustomWallpaper: Boolean,
    hasPending: Boolean,
    onSelectPhoto: () -> Unit,
    onUseDefault: () -> Unit,
    onApply: () -> Unit,
    onCancel: () -> Unit,
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val shape = RoundedCornerShape(24.dp)
        Row(
            Modifier
                .fillMaxWidth(0.8f)
                .background(Color(0xF2171A3F), shape)
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val previewShape = RoundedCornerShape(14.dp)
            Box(
                Modifier
                    .weight(1f)
                    .aspectRatio(16f / 9f)
                    .clip(previewShape)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), previewShape)
                    .semantics {
                        contentDescription =
                            if (hasPending) "Selected wallpaper preview, not applied yet" else "Current wallpaper preview"
                    },
            ) {
                if (preview != null) {
                    Image(
                        bitmap = preview,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    DefaultWallpaper(Modifier.fillMaxSize())
                }
            }
            Column(
                Modifier.widthIn(min = 150.dp, max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    "Wallpaper",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                TextButton("Select photo", fullWidth = true, onClick = onSelectPhoto)
                TextButton("Use default", enabled = hasCustomWallpaper || hasPending, fullWidth = true, onClick = onUseDefault)
                TextButton("Apply", primary = true, enabled = hasPending, fullWidth = true, onClick = onApply)
                TextButton("Cancel", fullWidth = true, onClick = onCancel)
            }
        }
    }
}

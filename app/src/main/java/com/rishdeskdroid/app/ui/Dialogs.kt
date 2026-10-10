package com.rishdeskdroid.app.ui

import android.appwidget.AppWidgetProviderInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import com.rishdeskdroid.app.data.AppInfo

private val DialogSurface = Color(0xF2171A3F)

@Composable
private fun PickerFrame(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            Modifier
                .fillMaxWidth(0.6f)
                .fillMaxHeight(0.88f)
                .background(DialogSurface, RoundedCornerShape(24.dp))
                .padding(vertical = 16.dp),
        ) {
            Text(
                title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
            Spacer(Modifier.height(6.dp))
            content()
        }
    }
}

/** Pick any installed app (used by the dock's + tile). */
@Composable
fun AppPickerDialog(
    title: String,
    apps: List<AppInfo>,
    onPick: (AppInfo) -> Unit,
    onDismiss: () -> Unit,
) {
    PickerFrame(title, onDismiss) {
        LazyColumn(Modifier.fillMaxWidth()) {
            items(apps, key = { it.key }) { app ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPick(app) }
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    AppIconImage(app, 40.dp)
                    Text(
                        app.label,
                        color = Color.White,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** Pick a widget to add to the home screen (used by Settings > Widgets > +). */
@Composable
fun WidgetPickerDialog(
    providers: List<AppWidgetProviderInfo>,
    onPick: (AppWidgetProviderInfo) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    PickerFrame("Select widget", onDismiss) {
        if (providers.isEmpty()) {
            Text(
                "No widgets available on this device.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.padding(20.dp),
            )
        }
        LazyColumn(Modifier.fillMaxWidth()) {
            items(providers, key = { "${it.provider.flattenToString()}#${it.user}" }) { info ->
                val label = remember(info) { info.loadLabel(context.packageManager) }
                val icon = remember(info) {
                    try {
                        info.loadIcon(context, 0)?.toBitmap(96, 96)?.asImageBitmap()
                    } catch (e: Exception) {
                        null
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPick(info) }
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    if (icon != null) {
                        Image(bitmap = icon, contentDescription = null, modifier = Modifier.size(40.dp))
                    } else {
                        Box(Modifier.size(40.dp).background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp)))
                    }
                    Text(
                        label,
                        color = Color.White,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

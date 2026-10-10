package com.rishdeskdroid.app.ui

import android.os.Bundle
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.rishdeskdroid.app.data.WidgetController

/** Shows one hosted Android app widget in a rounded frame. */
@Composable
fun WidgetView(controller: WidgetController, id: Int, modifier: Modifier = Modifier) {
    val info = remember(id) { controller.manager.getAppWidgetInfo(id) } ?: return
    val density = LocalDensity.current

    val minWidth = with(density) { info.minWidth.toDp() }
    val minHeight = with(density) { info.minHeight.toDp() }
    val width = minWidth.coerceIn(110.dp, 320.dp)
    val height = minHeight.coerceIn(80.dp, 200.dp)
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier
            .size(width, height)
            .clip(shape)
            .border(1.dp, Color.White.copy(alpha = 0.28f), shape),
    ) {
        AndroidView(
            factory = { ctx -> controller.host.createView(ctx, id, info) },
            update = { view ->
                view.updateAppWidgetSize(Bundle(), width.value.toInt(), height.value.toInt(), width.value.toInt(), height.value.toInt())
            },
            modifier = Modifier.size(width, height),
        )
    }
}

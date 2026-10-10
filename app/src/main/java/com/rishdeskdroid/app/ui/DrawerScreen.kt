package com.rishdeskdroid.app.ui

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rishdeskdroid.app.data.AddToHomeResult
import com.rishdeskdroid.app.data.AppInfo
import com.rishdeskdroid.app.data.LauncherPrefs
import com.rishdeskdroid.app.data.addToDock
import com.rishdeskdroid.app.data.addToHome
import com.rishdeskdroid.app.data.launchApp
import com.rishdeskdroid.app.data.openAppInfo
import com.rishdeskdroid.app.data.uninstallApp

private const val COLUMNS = 5
private const val ROWS = 3
private const val PER_PAGE = COLUMNS * ROWS

/**
 * The app drawer: all apps in a 5 x 3 grid per page, with page indicator dots at the bottom.
 * Tap empty space (or press back) to close it.
 */
@Composable
fun DrawerScreen(
    prefs: LauncherPrefs,
    apps: List<AppInfo>,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pages = remember(apps) {
        val chunks = apps.chunked(PER_PAGE)
        if (chunks.isEmpty()) listOf(emptyList<AppInfo>()) else chunks
    }
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column(
        modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClose,
            ),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { page ->
            AppPage(prefs, apps, pages[page])
        }
        PageDots(count = pages.size, current = pagerState.currentPage)
    }
}

@Composable
private fun AppPage(prefs: LauncherPrefs, allApps: List<AppInfo>, items: List<AppInfo>) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 4.dp),
    ) {
        for (row in 0 until ROWS) {
            Row(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                for (col in 0 until COLUMNS) {
                    val app = items.getOrNull(row * COLUMNS + col)
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (app != null) DrawerApp(prefs, allApps, app)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DrawerApp(prefs: LauncherPrefs, allApps: List<AppInfo>, app: AppInfo) {
    val context = LocalContext.current
    var menu by remember { mutableStateOf(false) }
    val source = remember { MutableInteractionSource() }

    Box {
        Column(
            Modifier
                .pressScale(source, 1.07f)
                .combinedClickable(
                    interactionSource = source,
                    indication = null,
                    onClick = { launchApp(context, app) },
                    onLongClick = { menu = true },
                )
                .semantics { contentDescription = app.label }
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            AppIconImage(app, 54.dp)
            Text(
                text = app.label,
                color = Color.White,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = LabelStyle,
                modifier = Modifier.padding(horizontal = 2.dp),
            )
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(
                text = { Text("Add to Home") },
                onClick = {
                    menu = false
                    val message = when (addToHome(prefs, allApps, app)) {
                        AddToHomeResult.Added -> "${app.label} added to Home"
                        AddToHomeResult.AlreadyOnHome -> "${app.label} is already on Home"
                        AddToHomeResult.Full -> "The home screen is full"
                        AddToHomeResult.Unavailable -> "Can't add ${app.label}"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                },
            )
            DropdownMenuItem(
                text = { Text("Add to dock") },
                onClick = {
                    menu = false
                    if (!addToDock(prefs, allApps, app)) {
                        Toast.makeText(context, "The dock is full", Toast.LENGTH_SHORT).show()
                    }
                },
            )
            DropdownMenuItem(
                text = { Text("App info") },
                onClick = {
                    menu = false
                    openAppInfo(context, app)
                },
            )
            DropdownMenuItem(
                text = { Text("Uninstall") },
                onClick = {
                    menu = false
                    uninstallApp(context, app)
                },
            )
        }
    }
}

@Composable
private fun PageDots(count: Int, current: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp, top = 4.dp)
            .semantics { contentDescription = "Page ${current + 1} of $count" },
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 0 until count) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = if (i == current) 1f else 0.4f)),
            )
        }
    }
}

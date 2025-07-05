package com.jujinkim.frequaw.ui

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.jujinkim.frequaw.R
import com.jujinkim.frequaw.SharedPref
import com.jujinkim.frequaw.applist.AppListManager
import com.jujinkim.frequaw.data.FrequawData
import com.jujinkim.frequaw.model.AppInfo
import java.text.SimpleDateFormat
import java.util.*

@Preview
@Composable
fun PreviewResetHistoryItem() {
    HistoryItemComposable(
        icon = null,
        title = "Title",
        packageName = "package.name",
        fill = 0.5f,
        onClick = {},
        onResetClick = {})
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsGeneralResetHistoryComposable(data: FrequawData) {
    var isTableShown by remember { mutableStateOf(false) }
    var selectedAppInfo by remember { mutableStateOf<AppInfo?>(null) }

    Column {
        val context = LocalContext.current

        Row {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.clear_app_history_not_in_list_not_launched)
            )
            IconButton(
                onClick = {
                    askAndClearAllAppsHistory(context) {
                        // todo : screen go back
                    }
                }
            ) {
                Image(
                    painter = painterResource(R.drawable.reset),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(if (isSystemInDarkTheme()) Color.DarkGray else Color.LightGray),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // On debug mode, show last reduced time
        if (SharedPref.getDebugMode()) {
            val lastReducedTime = SharedPref.getReduceLaunchedCntTimestamp()
            val lastReducedTimeStr = if (lastReducedTime == 0L) {
                "Never"
            } else {
                val date = Date(lastReducedTime)
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                format.format(date)
            }
            Text(
                modifier = Modifier.padding(8.dp),
                text = "Last reduced time : ${lastReducedTimeStr}"
            )
        }

        val items = remember { mutableStateListOf<AppInfo>() }
        items.addAll(
            data.appInfos
                .asSequence()
                .map { info ->
                    AppInfo(
                        info.packageName,
                        info.lastLaunched,
                        info.launchedCount.toLongArray()
                    ).apply {
                        sortValue = info.launchedCount.sum()
                    }
                }
                .filterNot { it.appName().isBlank() }
                .sortedBy { it.appName() }
        )

        val maxSortValue = items.maxOf { it.sortValue }.coerceAtLeast(1)

        LazyColumn {
            items(items = items) { appInfo ->
                HistoryItemComposable(
                    appInfo.icon(),
                    appInfo.appName(),
                    appInfo.packageName,
                    fill = appInfo.sortValue.toFloat() / maxSortValue,
                    onClick = {
                        if (SharedPref.getDebugMode()) {
                            selectedAppInfo = appInfo
                            isTableShown = true
                        }
                    },
                    onResetClick = {
                        askAndClearAppHistory(context, appInfo) { result ->
                            if (result) items.remove(appInfo)
                        }
                    }
                )
            }
        }
    }

    if (isTableShown) {
        SettingsGeneralResetHistoryCountTableComposable(
            appInfo = selectedAppInfo
        ) {
            selectedAppInfo = null
            isTableShown = false
        }
    }
}

@Composable
private fun HistoryItemComposable(
    icon: Drawable?,
    title: String,
    packageName: String,
    fill: Float,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onClick: () -> Unit,
    onResetClick: () -> Unit
) = Row(
    modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .clickable(
            onClick = onClick,
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        )
        .padding(8.dp)
        .drawBehind {
            drawRect(
                color = if (isDarkTheme) Color.DarkGray else Color.LightGray,
                size = Size(size.width * fill, size.height)
            )
        },
    verticalAlignment = Alignment.CenterVertically
) {
    Image(
        modifier = Modifier.size(36.dp),
        bitmap = icon?.toBitmap()?.asImageBitmap() ?: ImageBitmap(1, 1),
        contentDescription = null
    )
    Spacer(modifier = Modifier.width(8.dp))
    Column(
        modifier = Modifier.weight(1f)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            maxLines = 1
        )
        Text(
            text = packageName,
            fontSize = 12.sp,
            maxLines = 1,
        )
    }
    IconButton(
        onClick = onResetClick
    ) {
        Image(
            painter = painterResource(R.drawable.reset),
            colorFilter = ColorFilter.tint(if(isDarkTheme) Color.White else Color.Black),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}

private fun askAndClearAppHistory(context: Context, appInfo: AppInfo, onResult: (Boolean) -> Unit) {
    val appName = appInfo.appName()
    AlertDialog.Builder(context)
        .setTitle(
            context.getString(
                R.string.clear_app_history_reset_ps_history,
                appName
            )
        )
        .setMessage(
            context.getString(
                R.string.clear_app_history_are_you_sure_to_clear_this_app_ps,
                appName
            )
        )
        .setPositiveButton(com.jujinkim.frequaw.R.string.yes) { _, _ ->
            AppListManager.resetOneAppLaunchedInfo(appInfo.packageName)
            onResult(true)
            Toast.makeText(
                context,
                context.getString(
                    R.string.clear_app_history_ps_history_cleared,
                    appName
                ),
                Toast.LENGTH_SHORT
            ).show()
        }
        .setNegativeButton(R.string.no) { _, _ ->
            onResult(false)
        }
        .create()
        .show()
}

private fun askAndClearAllAppsHistory(context: Context, onResult: (Boolean) -> Unit) {
    val resetDialog = AlertDialog.Builder(context)
    resetDialog.setTitle(R.string.general_reset_app_list)
    resetDialog.setMessage(R.string.general_reset_app_list_warning)
    resetDialog.setNegativeButton(R.string.no) { dialog, _ ->
        dialog.dismiss()
        onResult(false)
    }
    resetDialog.setPositiveButton(
        R.string.yes
    ) { _, _ ->
        AppListManager.clearAllAppsLaunchedCount()
        Toast.makeText(
            context,
            R.string.general_reset_app_list_done,
            Toast.LENGTH_LONG
        ).show()
        onResult(true)
    }.create().show()
}
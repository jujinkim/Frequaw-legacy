package com.jujinkim.frequaw.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.jujinkim.frequaw.FrequawApp
import com.jujinkim.frequaw.R
import com.jujinkim.frequaw.applist.AppListManager
import com.jujinkim.frequaw.data.FrequawData
import com.jujinkim.frequaw.data.FrequawDataHelper
import com.jujinkim.frequaw.model.AppFilter
import com.jujinkim.frequaw.model.init
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsAppListPinAppComposable(data: FrequawData, widgetId: Int) = Box {
    var isInfoDialogOpened by rememberSaveable { mutableStateOf(false) }

    Column {
        val widgetData = data.getWidgetSetting(widgetId)
        var searchStr by rememberSaveable { mutableStateOf("") }

        val items = remember { mutableStateListOf<AppFilter>() }
        var isLoadingProgressShown by rememberSaveable { mutableStateOf(false) }

        Box(Modifier.fillMaxWidth().wrapContentHeight()) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = searchStr,
                onValueChange = { searchStr = it },
                label = { Text(stringResource(R.string.search_apps)) }
            )
            if (isLoadingProgressShown)
                CircularProgressIndicator(Modifier.align(Alignment.CenterEnd).padding(end = 8.dp))
        }

        PinAppInfoComposable { isInfoDialogOpened = true }

        LaunchedEffect(searchStr) {
            withContext(Dispatchers.Default) {
                isLoadingProgressShown = true

                val appLists = AppListManager.getSortedApps(widgetId)
                    .asSequence()
                    .map { appInfo -> appInfo.packageName
                    }
                    .toMutableList()

                appLists.addAll(
                    AppListManager.getInstalledApps()
                        .asSequence()
                        .filter { pkg -> appLists.find { it == pkg } == null }
                )

                val finalAppList = appLists
                    .asSequence()
                    .filterNot { pkg -> pkg == FrequawApp.appContext.packageName }
                    .map { pkg -> AppFilter(
                            pkg,
                            widgetData.pinnedApps.contains(pkg)
                        ).init()
                    }
                    .filter {
                        searchStr.isEmpty() ||
                                it.packageName.contains(searchStr, true) ||
                                it.appName.contains(searchStr, true)
                    }
                    .sortedBy { appFilter -> appFilter.appName }
                    .sortedByDescending { if (widgetData.pinnedApps.contains(it.packageName)) 1 else 0 }
                    .toMutableList()

                items.clear()
                items.addAll(finalAppList)

                isLoadingProgressShown = false
            }
        }

        LazyColumn {
            items(items = items) { appFilter ->
                ItemComposable(
                    icon = appFilter.icon?.toBitmap() ?: Bitmap.createBitmap(
                        1,
                        1,
                        Bitmap.Config.RGB_565
                    ),
                    name = appFilter.appName,
                    pkg = appFilter.packageName,
                    enabled = appFilter.isFilter,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            widgetData.pinnedApps.add(appFilter.packageName)
                        } else {
                            widgetData.pinnedApps.remove(appFilter.packageName)
                        }
                        appFilter.isFilter = enabled
                        FrequawDataHelper.saveWidgetSetting(widgetData)
                    }
                )
            }
        }
    }

    // Pin app info
    if (isInfoDialogOpened) {
        AlertDialog(
            text = { Text(stringResource(R.string.pin_app_desc)) },
            onDismissRequest = { isInfoDialogOpened = false },
            confirmButton = {
                Button(onClick = { isInfoDialogOpened = false }) {
                    Text(stringResource(R.string.okay))
                }
            }
        )
    }
}

@Composable
private fun ItemComposable(
    icon: Bitmap,
    name: String,
    pkg: String,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) = Row(
    modifier = Modifier.padding(4.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Image(
        bitmap = icon.asImageBitmap(),
        contentDescription = null,
        modifier = Modifier.size(48.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Column(Modifier.weight(1f)) {
        Text(
            text = name,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = pkg,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }

    var checkedState by remember { mutableStateOf(enabled) }
    Switch(
        checked = checkedState,
        onCheckedChange = {
            checkedState = it
            onCheckedChange(it)
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PinAppInfoComposable(
    onInfoButtonClick: () -> Unit
) = Row(
    modifier = Modifier
        .fillMaxWidth()
        .height(30.dp)
        .padding(5.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Text(
        modifier = Modifier.weight(1f),
        text = stringResource(R.string.pin_app_desc),
        fontSize = 12.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
        IconButton(
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth(),
            onClick = onInfoButtonClick,
        ) {
            Image(
                modifier = Modifier
                    .fillMaxHeight()
                    .wrapContentWidth(),
                painter = painterResource(R.drawable.info),
                contentDescription = null
            )
        }
    }
}
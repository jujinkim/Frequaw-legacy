package com.jujinkim.frequaw.ui

import android.appwidget.AppWidgetManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jujinkim.frequaw.R
import com.jujinkim.frequaw.Screen.*
import com.jujinkim.frequaw.data.FrequawData
import com.jujinkim.frequaw.data.FrequawWidgetSettingData
import com.jujinkim.frequaw.viewmodel.SettingViewModel

@Preview
@Composable
fun SettingsPreview() {

}

@Composable
fun SettingsComposable(
    data: FrequawData,
    widgetId: Int,
    viewModel: SettingViewModel = viewModel()
) = Column(modifier = Modifier.fillMaxSize()) {
    val screen = viewModel.currentScreen.collectAsState()

    val isWidgetSettingExisted =
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            data.widgetSettings.containsKey(widgetId)
        } else {
            if (!data.widgetSettings.containsKey(AppWidgetManager.INVALID_APPWIDGET_ID)) {
                data.widgetSettings[AppWidgetManager.INVALID_APPWIDGET_ID] =
                    FrequawWidgetSettingData.default(AppWidgetManager.INVALID_APPWIDGET_ID)
            }
            true
        }

    Surface(modifier = Modifier
        .fillMaxWidth()
        .weight(1f)) {
        when (screen.value) {
            AppList ->
                if (isWidgetSettingExisted) {
                    SettingsAppListComposable(data, widgetId, viewModel)
                } else {
                    SettingsNoSettingComposable(viewModel)
                }
            AppListSortMode ->
                if (isWidgetSettingExisted) {
                    SettingsAppListAppSortByComposable(data, widgetId) {
                        viewModel.updateMessageBarLayoutByChangingMode(it)
                    }
                } else {
                    SettingsNoSettingComposable(viewModel)
                }
            AppListFilterApp ->
                if (isWidgetSettingExisted) {
                    SettingsAppListFilterAppComposable(data, widgetId)
                } else {
                    SettingsNoSettingComposable(viewModel)
                }
            AppListPinApp ->
                if (isWidgetSettingExisted) {
                    SettingsAppListPinAppComposable(data, widgetId)
                } else {
                    SettingsNoSettingComposable(viewModel)
                }
            Visual ->
                if (isWidgetSettingExisted) {
                    SettingsVisualComposable(data, widgetId)
                } else {
                    SettingsNoSettingComposable(viewModel)
                }
            VisualIconStyle ->
                if (isWidgetSettingExisted) {
                    SettingsVisualIconStyleComposable(data, widgetId)
                } else {
                    SettingsNoSettingComposable(viewModel)
                }
            General -> SettingsGeneralComposable(data, widgetId)
            GeneralResetHistory -> SettingsGeneralResetHistoryComposable(data)
            About -> SettingsAboutComposable()
        }
    }

    BottomNavigation(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        BottomNavigationItem(
            selected = screen.value == AppList || screen.value == AppListSortMode || screen.value == AppListFilterApp || screen.value == AppListPinApp,
            onClick = { viewModel.navigate(AppList) },
            icon = { Icon(Icons.Rounded.Apps, stringResource(R.string.header_app_list)) }
        )
        BottomNavigationItem(
            selected = screen.value == Visual || screen.value == VisualIconStyle,
            onClick = { viewModel.navigate(Visual) },
            icon = { Icon(Icons.Rounded.Brush, stringResource(R.string.header_visualization)) }
        )
        BottomNavigationItem(
            selected = screen.value == General || screen.value == GeneralResetHistory,
            onClick = { viewModel.navigate(General) },
            icon = { Icon(Icons.Rounded.Settings, stringResource(R.string.header_general)) }
        )
        BottomNavigationItem(
            selected = screen.value == About,
            onClick = { viewModel.navigate(About) },
            icon = { Icon(Icons.Rounded.Info, stringResource(R.string.general_about)) }
        )
    }
}
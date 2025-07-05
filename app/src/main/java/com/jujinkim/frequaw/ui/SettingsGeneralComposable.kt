package com.jujinkim.frequaw.ui

import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jujinkim.frequaw.*
import com.jujinkim.frequaw.data.FrequawData
import com.jujinkim.frequaw.data.FrequawDataHelper
import com.jujinkim.frequaw.viewmodel.SettingViewModel

@Composable
fun SettingsGeneralComposable(
    data: FrequawData,
    widgetId: Int,
    viewModel: SettingViewModel = viewModel()
) {
    val context = LocalContext.current

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            SettingListTitleComposable(stringResource(R.string.header_general))
        }

        // Show and reset count of each apps that Frequaw collected
        item {
            SettingListItemDescriptionComposable(
                text = stringResource(R.string.general_reset_app_each),
            ) {
                viewModel.navigate(Screen.GeneralResetHistory)
            }
        }

        // Set HomeLauncher package name manually
        item {
            SettingListItemTextFieldComposable(
                text = stringResource(R.string.general_set_launcher_package_name),
                value = data.homeLauncherPackageName,
                placeHolder = PackageUtil.homeLauncherPackageName,
                onTextChanged = {
                    data.homeLauncherPackageName = it
                    FrequawDataHelper.save(data)
                }
            )
        }

        // Set Original quality icon
        item {
            SettingListItemToggleComposable(
                text = stringResource(R.string.general_use_original_quality_icon),
                description = stringResource(R.string.general_use_original_quality_icon_desc),
                checked = SharedPref.getUseOriginalQualityIcon(),
                onCheckedChange = {
                    SharedPref.setUseOriginalQualityIcon(it)
                    FrequawDataHelper.save(data)
                }
            )
        }

        // Accessibility service permission state
        item {
            SettingListItemPermissionCheckComposable(
                text = stringResource(R.string.general_accessibility),
                description = stringResource(R.string.general_accessibility_desc),
                granted = { PermissionManager.isAccessibilityAllowed(context) }
            ) {
                PermissionManager.openAccessibilitySettingDialog(context)
            }
        }

        // Usage data permission state
        item {
            SettingListItemPermissionCheckComposable(
                text = stringResource(R.string.general_usage_stats),
                description = stringResource(R.string.general_usage_stats_desc),
                granted = { PermissionManager.isUsageStatsAllowed(context) }
            ) {
                PermissionManager.openUsageStatsPermissionDialog(context)
            }
        }

        // Batter optimization disabled state
        item {
            SettingListItemPermissionCheckComposable(
                text = stringResource(R.string.general_battery_optimization),
                description = stringResource(R.string.general_battery_optimization_desc),
                granted = { PermissionManager.isBatteryOptimizationDisabled(context) }
            ) {
                PermissionManager.openDisableBatteryOptimizationDialog(context)
            }
        }

        // Erase every widget specific settings
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            item {
                SettingListItemDescriptionComposable(
                    text = stringResource(R.string.general_remove_every_widget_settings),
                    description = stringResource(R.string.general_remove_every_widget_settings_desc)
                ) {
                    viewModel.openRemoveEveryWidgetSettings(context)
                }
            }
        }

        // Export & Import setting
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            item {
                SettingListItemDescriptionComposable(
                    text = stringResource(R.string.general_export_import_setting),
                    description = stringResource(R.string.general_export_import_setting_desc)
                ) {
                    viewModel.openExportImportDialog(context)
                }
            }
        }

        // Open tutorial
        item {
            SettingListItemDescriptionComposable(
                text = stringResource(R.string.general_tutorial),
            ) {
                startActivity(context, Intent(context, TutorialActivity::class.java), null)
            }
        }
    }
}
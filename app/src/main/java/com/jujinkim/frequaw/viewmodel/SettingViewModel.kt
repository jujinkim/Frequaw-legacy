package com.jujinkim.frequaw.viewmodel

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.AndroidViewModel
import com.jujinkim.frequaw.*
import com.jujinkim.frequaw.data.FrequawDataHelper
import com.jujinkim.frequaw.ui.FinishActivityListener
import kotlinx.coroutines.flow.MutableStateFlow

class SettingViewModel(application: Application) : AndroidViewModel(application) {
    var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    val settingWidgetStr = MutableStateFlow(application.getString(R.string.default_setting))
    val batteryOptimizationLayoutVisibility = MutableStateFlow(View.GONE)
    val accessibilityPermissionLayoutVisibility = MutableStateFlow(View.GONE)
    val usageUsagePermissionLayoutVisibility = MutableStateFlow(View.GONE)
    val settingContextMenuVisibility = MutableStateFlow(View.GONE)
    val widgetPreviewVisibility = MutableStateFlow(View.GONE)

    val currentScreen = MutableStateFlow(Screen.AppList)

    val updateWidgetPreviewEvent = SingleLiveEvent<Unit?>()
    val openWidgetSettingMenuEvent = SingleLiveEvent<Unit?>()

    lateinit var finishActivityListener: FinishActivityListener

    lateinit var exportImportHelper: ExportImportHelper

    fun navigate(to: Screen) {
        currentScreen.value = to
    }

    fun navigateUp() {
        currentScreen.value = when (currentScreen.value) {
            Screen.AppListSortMode -> Screen.AppList
            Screen.AppListFilterApp -> Screen.AppList
            Screen.AppListPinApp -> Screen.AppList
            Screen.VisualIconStyle -> Screen.Visual
            Screen.GeneralResetHistory -> Screen.General
            else -> {
                finishActivityListener.onActivityFinishRequested()
                currentScreen.value
            }
        }
    }

    fun checkBatteryOptimizationState() {
        val isIgnored = SharedPref.getBatteryOptimizationMessageIgnored()
        val isBatteryOptimizeDisabled = PermissionManager.isBatteryOptimizationDisabled(getApplication())

        batteryOptimizationLayoutVisibility.value =
            if (!isIgnored && !isBatteryOptimizeDisabled) View.VISIBLE else View.GONE
    }

    fun updateMessageBarLayoutByChangingMode(mode: SortMode) {
        val isUsageStatsAllowed = PermissionManager.isUsageStatsAllowed(getApplication())
        val isAccessibilityOn = PermissionManager.isAccessibilityAllowed(getApplication())
        val isAccessibilityServiceRunning = PermissionManager.isServiceRunning(getApplication())

        accessibilityPermissionLayoutVisibility.value =
            if ((mode == SortMode.Count ||
                    mode == SortMode.Recommend ||
                    mode == SortMode.Recent) &&
                    !isAccessibilityOn && !isAccessibilityServiceRunning) View.VISIBLE
            else View.GONE
        usageUsagePermissionLayoutVisibility.value =
            if (mode == SortMode.Period && !isUsageStatsAllowed) View.VISIBLE
            else View.GONE
    }

    fun onAccessibilityLayoutClicked(context: Context) {
        PermissionManager.openAccessibilitySettingDialog(context)
    }

    fun onUsageStatsLayoutClicked(context: Context) {
        PermissionManager.openUsageStatsPermissionDialog(context)
    }

    fun onBatteryOptimizationLayoutClicked(context: Context) {
        PermissionManager.openDisableBatteryOptimizationDialog(context)
    }

    fun openExportImportDialog(context: Context) {
        AlertDialog.Builder(context)
            .setMessage(R.string.general_export_import_setting_desc)
            .setPositiveButton(R.string.general_export_import_setting_export) { _, _ ->
                exportImportHelper.export()
            }
            .setNegativeButton(R.string.general_export_import_setting_import) { _, _ ->
                exportImportHelper.import()
            }
            .setNeutralButton(R.string.cancel, null)
            .show()
    }

    fun openRemoveEveryWidgetSettings(context: Context) {
        AlertDialog.Builder(context)
            .setMessage(R.string.general_remove_every_widget_settings_confirm)
            .setPositiveButton(R.string.yes) { _, _ ->
                FrequawDataHelper.eraseAllSpecificWidgetSettings()
                Toast.makeText(context, R.string.done, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    fun openWidgetSettingContextMenu() {
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID)
            openWidgetSettingMenuEvent.call()
    }

    fun openDefaultSetting(activityIntent: Intent) {
        activityIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        finishActivityListener.onActivityFinishRequested()
        FrequawApp.appContext.startActivity(activityIntent)

    }

    fun createWidgetSetting(activityIntent: Intent) {
        FrequawDataHelper.saveWidgetSetting(
            FrequawDataHelper.loadWidgetSetting().copy(_widgetId = widgetId)
        )

        activityIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        finishActivityListener.onActivityFinishRequested()
        FrequawApp.appContext.startActivity(activityIntent)
    }

    fun resetToDefaultSetting(context: Context, activityIntent: Intent) {
        AlertDialog.Builder(context)
            .setMessage(context.getString(R.string.reset_to_default_setting_confirm_pd, widgetId))
            .setPositiveButton(R.string.yes) { _, _ ->
                val defSetting = FrequawDataHelper.loadWidgetSetting().copy(_widgetId = widgetId)
                FrequawDataHelper.saveWidgetSetting(defSetting)

                activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                finishActivityListener.onActivityFinishRequested()
                FrequawApp.appContext.startActivity(activityIntent)
            }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .show()
    }

    fun eraseThisWidgetSetting(context: Context, activityIntent: Intent) {
        AlertDialog.Builder(context)
            .setMessage(context.getString(R.string.erase_this_widget_setting_confirm_pd, widgetId))
            .setPositiveButton(R.string.yes) { _, _ ->
                FrequawDataHelper.eraseWidgetSetting(widgetId)

                activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                finishActivityListener.onActivityFinishRequested()
                FrequawApp.appContext.startActivity(activityIntent)
            }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .show()

    }
}
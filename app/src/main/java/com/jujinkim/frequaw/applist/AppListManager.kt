package com.jujinkim.frequaw.applist

import android.appwidget.AppWidgetManager
import android.content.pm.PackageManager
import com.jujinkim.frequaw.*
import com.jujinkim.frequaw.data.FrequawDataHelper
import com.jujinkim.frequaw.data.FrequawWidgetSettingData
import com.jujinkim.frequaw.model.AppInfo
import java.text.SimpleDateFormat
import java.util.*

object AppListManager {
    private fun getAppListRepo(widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID) : AppListRepo {
        val widgetSettingData =
            FrequawDataHelper.load().widgetSettings[widgetId] ?: FrequawWidgetSettingData.default(widgetId)
        return when (widgetSettingData.sortAppBy) {
            SortMode.Count -> accessibilityAppListRepo
            SortMode.Period -> usageStatsAppListRepo
            SortMode.Recommend ->
                when (widgetSettingData.recommendSortMode) {
                    RcmdSortMode.Time -> recommendTimeAppListRepo
                    RcmdSortMode.Balanced -> recommendBalancedAppListRepo
                    RcmdSortMode.Count -> recommendCountAppListRepo
            }
            SortMode.Recent -> recentAppListRepo
        }
    }

    val accessibilityAppListRepo = AppListRepoAccessibilityService()
    private val usageStatsAppListRepo = AppListRepoUsageStats()
    private val recommendTimeAppListRepo = AppListRepoRecommend(accessibilityAppListRepo, RcmdSortMode.Time)
    private val recommendBalancedAppListRepo = AppListRepoRecommend(accessibilityAppListRepo, RcmdSortMode.Balanced)
    private val recommendCountAppListRepo = AppListRepoRecommend(accessibilityAppListRepo, RcmdSortMode.Count)
    private val recentAppListRepo = AppListRepoRecent(accessibilityAppListRepo)

    var lastUpdateTime = "--" ; private set
    val lastLoadedAppList = mutableListOf<AppInfo>()

    fun getSortedApps(widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID): List<AppInfo> {
        val commonData = FrequawDataHelper.load()
        val widgetSettingData = FrequawDataHelper.loadWidgetSetting(widgetId)

        lastUpdateTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        val apps = getAppListRepo(widgetId).getAppList()
        if (apps.isEmpty()) return lastLoadedAppList

        val filter = getFilterList(widgetId)
        val isBlockList = widgetSettingData.filterMode == FilterMode.BlockList
        val pinAppList = widgetSettingData.pinnedApps

        return apps
            .asSequence()
            .filterNot { appFilter -> appFilter.packageName == FrequawApp.appContext.packageName }
            .filterNot { appFilter -> appFilter.packageName == PackageUtil.homeLauncherPackageName }
            .filter { app ->
                // Filter app
                if (isBlockList) !filter.contains(app.packageName)
                else filter.contains(app.packageName)
            }.filter {
                val pm = FrequawApp.appContext.packageManager
                pm.getLaunchIntentForPackage(it.packageName) != null
            }.sortedByDescending { app ->
                app.sortValue
            }.sortedByDescending { app ->
                // Pin App
                pinAppList.contains(app.packageName)
            }.toList()
            .also { lastLoadedAppList.clear(); lastLoadedAppList.addAll(it) }
    }

    fun resetOneAppLaunchedInfo(packageName: String) {
        accessibilityAppListRepo.resetOnAppInfo(packageName)
    }

    private fun getFilterList(widgetId: Int) : List<String> {
        val widgetSettingData = FrequawDataHelper.loadWidgetSetting(widgetId)
        return when (widgetSettingData.filterMode == FilterMode.BlockList) {
            true -> widgetSettingData.blockApps.map { it }
            false -> widgetSettingData.allowApps.map { it }
        }.toMutableList()
    }


    fun getInstalledApps() = FrequawApp.appContext
        .packageManager
        .getInstalledApplications(PackageManager.GET_META_DATA)
        .filter {
            FrequawApp.appContext.packageManager.getLaunchIntentForPackage(
                it.packageName
            ) != null
        }
        .map { it.packageName }

    /**
     * Clear and reset app info list, save cleared app info list into the shared pref
     */
    fun clearAllAppsLaunchedCount() {
        accessibilityAppListRepo.clearAppList()
    }
}
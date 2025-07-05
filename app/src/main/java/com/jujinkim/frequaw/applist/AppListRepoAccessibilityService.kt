package com.jujinkim.frequaw.applist

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.jujinkim.frequaw.FrequawApp
import com.jujinkim.frequaw.data.FrequawDataHelper
import com.jujinkim.frequaw.model.AppInfo
import com.jujinkim.frequaw.model.AppInfo.Companion.launchedCountsBy30mSize
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.max

class AppListRepoAccessibilityService : AppListRepo {
    private var lastUpdatedPackage = ""
    var lastUpdateTime = "--" ; private set

    override fun getAppList(): MutableList<AppInfo> {
        Log.d(FrequawApp.TAG_DEBUG, "Get App List from AccessibilityService Repo")

        val apps: MutableList<AppInfo> = mutableListOf()
        apps.addAll(FrequawDataHelper.load().appInfos.map { AppInfo.fromData(it) })
        if (apps.isEmpty()) apps.addAll(createInitAppInfoList())

        apps.forEach { it.sortValue = it.launchedTimes }

        return apps
    }

    override fun updateAndSaveAppInfo(packageName: String,
                                      lastLaunched: Long,
                                      ignoreInMomentRequest: Boolean) {

        val apps = getAppList()

        val appInfo = apps.firstOrNull { info -> info.packageName == packageName }

        // if the same package requested to be updated between very short time, ignore it.
        if (ignoreInMomentRequest && appInfo != null) {
            if (abs(appInfo.lastLaunched - lastLaunched) < 500) return // 0.5sec is enough
        }

        if (appInfo == null) {
            apps.add(AppInfo(packageName, lastLaunched, LongArray(launchedCountsBy30mSize)))
            lastUpdatedPackage = packageName
        } else if (appInfo.packageName != lastUpdatedPackage) {
            appInfo.lastLaunched = lastLaunched
            lastUpdatedPackage = packageName

            // update launched count by every 30 minutes
            val cal = Calendar.getInstance()
            val curDayOfWeek = cal[Calendar.DAY_OF_WEEK] - 1
            val curHour = cal[Calendar.HOUR_OF_DAY]
            val curMinute = cal[Calendar.MINUTE]
            val idx30m = curDayOfWeek * 48 + curHour * 2 + curMinute / 30
            appInfo.launchedCountsBy30m[idx30m] =
                (appInfo.launchedCountsBy30m[idx30m] + 1).coerceAtMost(Long.MAX_VALUE)

            // remove duplicated
            apps.filter { info -> info.packageName == appInfo.packageName }
                .takeIf { it.size >= 2 }
                ?.forEachIndexed { index, unusedAppInfo ->
                    if (index > 0) {
                        // integrate two appInfo
                        apps[0].lastLaunched = max(apps[0].lastLaunched, unusedAppInfo.lastLaunched)
                        for (i in 0 until apps[0].launchedCountsBy30m.size) {
                            if (i >= unusedAppInfo.launchedCountsBy30m.size) break
                            apps[0].launchedCountsBy30m[i] =
                                (apps[0].launchedCountsBy30m[i] + unusedAppInfo.launchedCountsBy30m[i])
                                    .coerceAtMost(Long.MAX_VALUE)
                        }
                        apps.remove(unusedAppInfo)
                    }
                }
            Log.d(FrequawApp.TAG_DEBUG, "APP Updated: $packageName")
        }

        saveAppList(apps)

        lastUpdateTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }

    private var isWaitToSaveAppList = false
    override fun saveAppList(apps: List<AppInfo>) {
        if (isWaitToSaveAppList) return
        isWaitToSaveAppList = true

        Handler(Looper.getMainLooper()).postDelayed({
            FrequawDataHelper
                .load()
                .apply {
                    appInfos.clear()
                    appInfos.addAll(apps.map { it.toData() })
                }.run {
                    FrequawDataHelper.save(this)
                }
            isWaitToSaveAppList = false
            },1000  // don't save too frequently
        )
    }

    override fun clearAppList() {
        FrequawDataHelper
            .load()
            .apply {
                appInfos.clear()
            }.run {
                FrequawDataHelper.save(this)
            }
    }

    override fun resetOnAppInfo(packageName: String) {
        val apps = getAppList()
        val appInfo = apps.find { appInfo -> appInfo.packageName == packageName }

        appInfo?.apply {
            lastLaunched = 0
            for(i in launchedCountsBy30m.indices) {
                launchedCountsBy30m[i] = 0
            }
        }

        saveAppList(apps)
        lastUpdateTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }

    /**
     * Create a list of AppInfo, all installed applications but no launched time info
     */
    private fun createInitAppInfoList() =
        AppListManager.getInstalledApps()
            .map { packageName -> AppInfo(packageName, 0, LongArray(launchedCountsBy30mSize)) }
}
package com.jujinkim.frequaw.applist

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import com.jujinkim.frequaw.FrequawApp
import com.jujinkim.frequaw.PermissionManager
import com.jujinkim.frequaw.model.AppInfo
import java.util.*

class AppListRepoUsageStats : AppListRepo {

    override fun getAppList(): List<AppInfo> {
        Log.d(FrequawApp.TAG_DEBUG, "Get App List from UsageStats Repo")

        if (!PermissionManager.isUsageStatsAllowed(FrequawApp.appContext)) {
            //Toast.makeText(FrequawApp.appContext, R.string.usage_stats_permission_not_granted, Toast.LENGTH_SHORT).show()
            Log.d(FrequawApp.TAG_DEBUG, "Fail to get UsageStats list : No permission")
            return listOf()
        }

        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }  // Period = 1 month

        val usageStatsManager =
            FrequawApp.appContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            cal.timeInMillis,
            System.currentTimeMillis()
        ) ?: listOf()

        return stats
            .groupBy({it.packageName}, {it})
            .mapValues { (pkg, stats) ->
                AppInfo(
                    pkg,
                    stats.map { it.lastTimeUsed }.reduce { a, b -> a + b },
                    LongArray(7)
                ).apply {
                    stats.forEach { stat ->
                        val time = (stat.firstTimeStamp * .5 + stat.lastTimeStamp * .5).toLong()
                        val day = Calendar.getInstance().apply { timeInMillis = time }.get(Calendar.DAY_OF_WEEK) - 1
                        launchedCountsBy30m[day] += stat.totalTimeInForeground
                    }

                    sortValue = launchedTimes
                }
            }
            .values
            .toList()
    }

    // Not support in the usage stats mode
    override fun updateAndSaveAppInfo(packageName: String,
                                      lastLaunched: Long,
                                      ignoreInMomentRequest: Boolean) {}

    // Not support in the usage stats mode
    override fun saveAppList(apps: List<AppInfo>) {}

    // Not support in the usage stats mode
    override fun clearAppList() {}

    // Not support in the usage stats mode
    override fun resetOnAppInfo(packageName: String) {}
}
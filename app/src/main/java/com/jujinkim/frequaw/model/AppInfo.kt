package com.jujinkim.frequaw.model

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import com.jujinkim.frequaw.FrequawApp
import com.jujinkim.frequaw.data.FrequawAppInfoData
import java.util.*

data class AppInfo(
    val packageName: String,
    var lastLaunched: Long,
    val launchedCountsBy30m: LongArray // size: 24(day) * 2(30m) * 7(week), or 7(week) for usage stats
) {
    var sortValue = 0L
    val launchedTimes: Long
    get() = launchedCountsBy30m.sum()

    var iconCache: Drawable? = null
    var nameCache: String? = null

    fun icon(): Drawable? {
        if (iconCache != null) return iconCache

        val context = FrequawApp.appContext
        iconCache = try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
        return iconCache
    }

    fun appName(): String {
        if (nameCache != null) return nameCache ?: ""

        val context = FrequawApp.appContext
        nameCache = try {
            context.packageManager.getApplicationLabel(
                context.packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.GET_META_DATA)
            ).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }

        return nameCache ?: ""
    }

    fun convertLaunchedTimeString(toFormatted: Boolean) : String {
        return if (toFormatted) {
            val seconds = (sortValue / 1000).toInt() % 60
            val minutes = (sortValue / (1000 * 60)).toInt() % 60
            val hours = (sortValue / (1000 * 60 * 60)).toInt()

            if (hours > 0) "${hours}h ${minutes}m ${seconds}s"
            else "${minutes}m ${seconds}s"
        } else {
            sortValue.toString()
        }
    }

    fun getCountOf30m(dayOfWeek: Int, hour: Int, half: Int): Long {
        if (dayOfWeek < 0 || dayOfWeek > 6 || hour < 0 || hour > 23 || half < 0 || half > 1) {
            Log.e("AppInfo", "getCountOf30m] Invalid parameters. $dayOfWeek $hour $half")
            return -1
        }

        val index = dayOfWeek * 48 + hour * 2 + half
        return launchedCountsBy30m[index]
    }

    fun getCurrentTimeCountOf30m(): Long {
        val cal = Calendar.getInstance()
        val curDayOfWeek = cal[Calendar.DAY_OF_WEEK] - 1
        val curHour = cal[Calendar.HOUR_OF_DAY]
        val curMinute = cal[Calendar.MINUTE]
        return getCountOf30m(curDayOfWeek, curHour, curMinute / 30)
    }

    fun toData() = FrequawAppInfoData(
        packageName,
        lastLaunched,
        launchedCountsBy30m.toList()
    )

    companion object {
        const val launchedCountsBy30mSize = 24 * 2 * 7

        fun fromData(data: FrequawAppInfoData) = AppInfo(
            data.packageName,
            data.lastLaunched,
            data.launchedCount.toLongArray()
        )
    }
}
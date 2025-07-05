package com.jujinkim.frequaw.applist

import com.jujinkim.frequaw.RcmdSortMode
import com.jujinkim.frequaw.getCyclic
import com.jujinkim.frequaw.model.AppInfo
import java.util.*
import kotlin.math.roundToLong

class AppListRepoRecommend(
    private val repoAccessibilityService: AppListRepoAccessibilityService,
    private val recommendMode: RcmdSortMode
) : AppListRepo by repoAccessibilityService {
    override fun getAppList(): List<AppInfo> {
        val accAppList = repoAccessibilityService.getAppList()

        val cal = Calendar.getInstance()
        val curDayOfWeek = cal[Calendar.DAY_OF_WEEK] - 1
        val curHour = cal[Calendar.HOUR_OF_DAY]
        val curMinute = cal[Calendar.MINUTE]

        accAppList.sortedByDescending {
            it.launchedTimes
        }.forEachIndexed { idx, appInfo ->
            // Set sortValue using launched counts.
            appInfo.sortValue = 0

            val wCurIdx30m = curDayOfWeek * 48 + curHour * 2 + curMinute / 30

            // If current 1.5 hours' counts are larger than other times in 10.5 hours
            // prev 4 hours, current 1.5 hours, next 4 hours
            // prev : curIdx - 8 ~ curIdx - 2, curIdx : curIdx -1 ~ curIdx + 1, next : curIdx + 2 ~ curIdx + 8
            val todayRange = wCurIdx30m - 8..wCurIdx30m + 8
            var todaySum = 0L
            var todayAverage = 0.0
            todayRange.forEach { idx30m -> todaySum += appInfo.launchedCountsBy30m.getCyclic(idx30m) }
            todayAverage = todaySum / todayRange.count().toDouble()
            val curTimes = listOf(
                appInfo.launchedCountsBy30m.getCyclic(wCurIdx30m),
                appInfo.launchedCountsBy30m.getCyclic(wCurIdx30m + 1),
                appInfo.launchedCountsBy30m.getCyclic(wCurIdx30m - 1)
            )
            val curTimeAverage = curTimes.average()
            var todaySortValue = (curTimeAverage / todayAverage).takeIf { it.isFinite() } ?: 0.0
            if (todaySum >= todayLaunchCountThreshold) todaySortValue *= todaySortValueOverWeight

            // If current 1.5 hours' counts (+ other days at same time) are larger than other all times in whole week
            val totalSum = appInfo.launchedCountsBy30m.sum()
            val totalAverage = appInfo.launchedCountsBy30m.average()
            var currentTimeAverageInTotal = 0.0
            for (i in 0 until 7) {
                currentTimeAverageInTotal += appInfo.launchedCountsBy30m.getCyclic(wCurIdx30m + i * 48)
                currentTimeAverageInTotal += appInfo.launchedCountsBy30m.getCyclic(wCurIdx30m + i * 48 + 1)
                currentTimeAverageInTotal += appInfo.launchedCountsBy30m.getCyclic(wCurIdx30m + i * 48 - 1)
            }
            currentTimeAverageInTotal /= 7.0
            var totalSortValue = (currentTimeAverageInTotal / totalAverage).takeIf { it.isFinite() } ?: 0.0
            if (totalSum >= totalLaunchCountThreshold) totalSortValue *= totalSortValueOverWeight

            appInfo.sortValue = (todaySortValue + totalSortValue).roundToLong()

            // Give priority by recommendMode
            var launchedCountBonus =
                ((maxLaunchedIndexToCompensate - idx)/maxLaunchedIndexToCompensate.toDouble()).coerceAtLeast(.0)
            launchedCountBonus *= when (recommendMode) {
                RcmdSortMode.Balanced -> launchedCountCompensate_Balanced
                RcmdSortMode.Time -> launchedCountCompensate_TimePriority
                RcmdSortMode.Count -> launchedCountCompensate_LaunchedCount
            }
            launchedCountBonus += 1

            appInfo.sortValue = (appInfo.sortValue * launchedCountBonus).roundToLong()

            // If current 1.5 hours' counts are not enough, make them reduced
            if (curTimes.sum() < todayLaunchCountAtLeast) {
                appInfo.sortValue = (appInfo.sortValue * todaySortValueLessWeight).roundToLong()
            }
        }

        return accAppList
    }

    companion object {
        const val todaySortValueOverWeight = 2.0
        const val totalSortValueOverWeight = 1.5
        const val todaySortValueLessWeight = 0.4
        const val todayLaunchCountThreshold = 5 // if launched count in 'today range' is more than this, give more weight
        const val totalLaunchCountThreshold = 12 // if launched count in 'total range' is more than this, give more weight
        const val todayLaunchCountAtLeast = 3 // if launch count in 'today range' is less then this, give less weight

        const val maxLaunchedIndexToCompensate = 30 // if the index of list that sorted by launched count is within this, this will be given more sortValue
        const val launchedCountCompensate_TimePriority = 0
        const val launchedCountCompensate_Balanced = 1
        const val launchedCountCompensate_LaunchedCount = 3
    }
}
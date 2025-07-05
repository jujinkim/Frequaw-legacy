package com.jujinkim.frequaw

import android.content.Context


object SharedPref {
    const val PREF_NAME = "FREQUAW_SHARED_PREF"

    private const val KEY_NOT_FIRST_RUN = "APP_NOT_FIRST_RUN"
    private const val KEY_FIRST_RUN_TIMESTAMP = "FIRST_RUN_TIMESTAMP"
    private const val KEY_REVIEW_REQUESTED = "APP_REVIEW_REQUESTED"
    private const val KEY_ADDED_WIDGET_ID_LIST = "APP_ADDED_WIDGET_ID_LIST"
    private const val KEY_REDUCE_LAUNCHED_CNT_TIMESTAMP = "APP_REDUCE_LAUNCHED_CNT_TIMESTAMP"
    private const val KEY_USE_ORIGINAL_QUALITY_ICON = "APP_USE_ORIGINAL_QUALITY_ICON"
    private const val KEY_DEBUG_MODE = "APP_DEBUG_MODE"
    private const val reviewRequiredTimeout = 5 * 24 * 60 * 60 * 1000   // 5 days

    private const val KEY_BATTERY_OPTIMIZATION_DISABLED_MSG_IGNORED = "BATTERY_OPTIMIZATION_DISABLED_MSG_IGNORED"

    fun getAndUpdateFirstRun() : Boolean {
        val isFirstRun = !getPref().getBoolean(KEY_NOT_FIRST_RUN, false)

        if (isFirstRun) {
            with(getPref().edit()) {
                putBoolean(KEY_NOT_FIRST_RUN, true)
                putLong(KEY_FIRST_RUN_TIMESTAMP, System.currentTimeMillis())
                apply()
            }
        }

        return isFirstRun
    }

    fun getAndUpdateReviewRequest() : Boolean {
        var firstLaunchedTime = getPref().getLong(KEY_FIRST_RUN_TIMESTAMP, -1)
        if (firstLaunchedTime == -1L) {
            firstLaunchedTime = Long.MAX_VALUE - reviewRequiredTimeout
            getPref()
                .edit()
                .putLong(KEY_FIRST_RUN_TIMESTAMP, System.currentTimeMillis())
                .apply()
        }

        val isTimeStampPass =
            firstLaunchedTime + reviewRequiredTimeout < System.currentTimeMillis()

        val isRequestable =
            !getPref().getBoolean(KEY_REVIEW_REQUESTED, false) && isTimeStampPass

        if (isRequestable) {
            getPref().edit().putBoolean(KEY_REVIEW_REQUESTED, true).apply()
        }

        return isRequestable
    }

    fun setBatteryOptimizationMessageIgnored(ignored: Boolean) =
        getPref().edit().putBoolean(KEY_BATTERY_OPTIMIZATION_DISABLED_MSG_IGNORED, ignored).apply()

    fun getBatteryOptimizationMessageIgnored() =
        getPref().getBoolean(KEY_BATTERY_OPTIMIZATION_DISABLED_MSG_IGNORED, false)

    private fun getPref() =
        FrequawApp.appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun setAddedWidget(add: Boolean, widgetId: Int) {
        val curAddedState = (getPref().getStringSet(KEY_ADDED_WIDGET_ID_LIST, emptySet()) ?: emptySet()).toMutableSet()
        if (add) {
            curAddedState.add(widgetId.toString())
            getPref().edit().putStringSet(KEY_ADDED_WIDGET_ID_LIST, curAddedState).apply()
        } else {
            curAddedState.remove(widgetId.toString())
            getPref().edit().putStringSet(KEY_ADDED_WIDGET_ID_LIST, curAddedState).apply()
        }
    }

    fun getAddedWidget(widgetId: Int) =
        getPref().getStringSet(KEY_ADDED_WIDGET_ID_LIST, emptySet())?.contains(widgetId.toString()) ?: false

    fun getReduceLaunchedCntTimestamp() =
        getPref().getLong(KEY_REDUCE_LAUNCHED_CNT_TIMESTAMP, 0)

    fun setReduceLaunchedCntTimestamp(timestamp: Long) =
        getPref().edit().putLong(KEY_REDUCE_LAUNCHED_CNT_TIMESTAMP, timestamp).apply()

    fun getUseOriginalQualityIcon() =
        getPref().getBoolean(KEY_USE_ORIGINAL_QUALITY_ICON, false)

    fun setUseOriginalQualityIcon(use: Boolean) =
        getPref().edit().putBoolean(KEY_USE_ORIGINAL_QUALITY_ICON, use).apply()

    fun getDebugMode() =
        getPref().getBoolean(KEY_DEBUG_MODE, false)

    fun toggleDebugMode(): Boolean {
        val debugMode = getDebugMode()
        getPref().edit().putBoolean(KEY_DEBUG_MODE, !debugMode).apply()
        return !debugMode
    }
}
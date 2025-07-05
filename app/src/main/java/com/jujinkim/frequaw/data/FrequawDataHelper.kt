package com.jujinkim.frequaw.data

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.jujinkim.frequaw.FrequawApp
import com.jujinkim.frequaw.RcmdSortMode
import com.jujinkim.frequaw.SharedPref

object FrequawDataHelper {
    // Half reduce interval 7 days
    const val HALF_REDUCE_INTERVAL = 1000 * 60 * 60 * 24 * 7

    const val PREF_NAME = "FREQUAW_SHARED_PREF"
    const val PREF_KEY = "FREQUAW_DATA"
    const val BACKUP_PREVIOUS_NORMAL = "_BACKUP_PREVIOUS_NORMAL"

    private var cachedLoadedData: FrequawData = load()
    private var cachedLoadedDataTimestamp: Long = 0
    private const val cachedLoadDataTtl: Long = 1000

    fun save(data: FrequawData) {
        // Check is half reduce needed
        val now = System.currentTimeMillis()
        if (now - SharedPref.getReduceLaunchedCntTimestamp() > HALF_REDUCE_INTERVAL) {
            SharedPref.setReduceLaunchedCntTimestamp(now)
            data.reduceLaunchedCounts()
        }

        val jsonStr = Gson().toJson(data)
        getPref().edit().putString(PREF_KEY, jsonStr).apply()
        cachedLoadedDataTimestamp = 0
    }

    fun saveWidgetSetting(data: FrequawWidgetSettingData) {
        val widgetId = data.widgetId
        val newData = data.copy()
        load().apply {
            widgetSettings[widgetId] = newData
            save(this)
        }
    }

    fun eraseWidgetSetting(widgetId: Int) {
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        load().apply {
            widgetSettings.remove(widgetId)
            save(this)
        }
    }

    fun eraseAllSpecificWidgetSettings() {
        load().apply {
            val def = getWidgetSetting(AppWidgetManager.INVALID_APPWIDGET_ID)
            widgetSettings.clear()
            widgetSettings[AppWidgetManager.INVALID_APPWIDGET_ID] = def
            save(this)
        }
    }

    fun load(): FrequawData {
        backUpPreviousValidData()

        // get cachedLoadedData if it is not too old
        if (System.currentTimeMillis() - cachedLoadedDataTimestamp < cachedLoadDataTtl) {
            return cachedLoadedData
        }

        val jsonStr = getPref().getString(PREF_KEY, null)
        val newLoadedData = gsonFromJsonAndFitVersion(jsonStr)
        return newLoadedData.also {
            cachedLoadedData = it
            cachedLoadedDataTimestamp = System.currentTimeMillis()
        }
    }

    fun loadWidgetSetting(widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID) : FrequawWidgetSettingData {
        return load().getWidgetSetting(widgetId)
    }

    private fun getPref(): SharedPreferences {
        return FrequawApp.appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun gsonFromJsonAndFitVersion(jsonStr: String?): FrequawData {
        // Check jsonStr is obfuscated v1
        val data = if (
            jsonStr?.contains("\"a\":") == true &&  // old app info list
            jsonStr?.contains("\"b\":") == true     // old widget setting list
        ) {
            Log.i(FrequawApp.TAG_DEBUG, "Obfuscated v1 data detected. Trying to restore...")
            FrequawOldDataUpgrader.restoreFromV1ObfuscatedData(jsonStr)
        } else {
            Gson().fromJson(jsonStr, FrequawData::class.java)
        }

        if (data == null) {
            Log.e("FrequawDataHelper", "Failed to load data from json string")
            return FrequawData.default()
        }

        // Version 1 -> 2
        upgradeVersion1to2(data)

        // Add more version upgrader here
        /*
        if (data.dataVersion == 2) {
            // Version 2 -> 3

            do something here

            data.setVersion(3)
        }
        */

        return data
    }

    private fun upgradeVersion1to2(data: FrequawData) {
        data.widgetSettings.values.forEach { setting ->
            if (setting.recommendSortMode == null) {
                setting.recommendSortMode = RcmdSortMode.Balanced
            }
        }
        if (data.dataVersion == 1) data.setVersion(2)
    }

    // This method is for the emergency case when the data is corrupted or unable to read.
    private fun backUpPreviousValidData() {
        val jsonStr = getPref().getString(PREF_KEY, null)
        if (jsonStr != null && jsonStr.length > 100) // 100 is just a random number. It should be enough to check if it is not empty and valid.
            getPref().edit().putString(PREF_KEY + BACKUP_PREVIOUS_NORMAL, jsonStr).apply()
    }
}
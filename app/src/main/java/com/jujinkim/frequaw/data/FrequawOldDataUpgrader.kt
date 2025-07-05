package com.jujinkim.frequaw.data

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Color
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.jujinkim.frequaw.*
import com.jujinkim.frequaw.model.AppInfo
import kotlin.math.roundToLong

// Upgrade Old Frequaw preference shared pref data which doesn't contain "appVersionCode" to new one
object FrequawOldDataUpgrader {
    private const val PREFIX_EXPORT_DEFAULT = 'd'
    private const val PREFIX_EXPORT_FREQUAW = 'f'
    const val PREF_NAME = "FREQUAW_SHARED_PREF"

    // Old custom pref keys
    private const val KEY_APP_BLOCK_FILTER_LIST = "APP_BLOCK_FILTER" // Filtered list save format : (0 or 1)PackageName
    private const val KEY_APP_ALLOW_FILTER_LIST = "APP_ALLOW_FILTER" // eg: 0com.jujinkim.appa, 1com.jujinkim.appb
    private const val KEY_APP_FILTER_MODE_IS_BLOCK = "APP_FILTER_MODE_IS_BLOCK"
    private const val KEY_PIN_APP_FIRST_LIST = "PIN_APP_FIRST_LIST"
    private const val KEY_APP_DETECT_MODE = "general_detect_mode"
    private const val KEY_APP_INFO_LIST = "APP_INFO"
    private const val KEY_IS_PRO_MODE = "IS_PRO_MODE"
    private const val KEY_ICON_STYLE = "ICON_STYLE"
    private const val KEY_ICON_PACK_PKG = "ICON_PACK_PKG"
    private const val KEY_ICON_SHAPE_FORCE = "ICON_SHAPE_FORCE"
    private const val KEY_SORTING_DIRECTION = "app_list_sort_direction"
    private const val KEY_IS_TITLE_VISIBLE = "visual_title_visible"
    private const val KEY_IS_TITLE_UPDATE_TIME_VISIBLE = "visual_title_update_time_visible"
    private const val KEY_TITLE_TEXT = "visual_title_text"
    private const val KEY_VISUAL_ICON_SIZE = "visual_icon_size"
    private const val KEY_IS_VISUAL_ADVANCED_ENABLED = "visual_advanced_enabled"
    private const val KEY_IS_VISUAL_ICON_GAP_SET_SEPARATE = "visual_icon_gap_set_separately"
    private const val KEY_VISUAL_ICON_GAP_MARGIN = "visual_icon_gap_margin"
    private const val KEY_VISUAL_ICON_GAP_VERTICAL_MARGIN = "visual_icon_gap_vertical_margin"
    private const val KEY_IS_VISUAL_LIST_MARGIN_SET_SEPARATE = "visual_list_margin_set_separately"
    private const val KEY_VISUAL_LIST_MARGIN = "visual_list_margin"
    private const val KEY_VISUAL_LIST_VERTICAL_MARGIN = "visual_list_vertical_margin"
    private const val KEY_IS_VISUAL_SHOW_APP_NAME = "visual_show_app_name"
    private const val KEY_VISUAL_APP_NAME_TEXT_SIZE = "visual_app_name_size"
    private const val KEY_VISUAL_APP_NAME_MARGIN = "visual_app_name_margin"
    private const val KEY_VISUAL_BG_CORNER_RADIUS = "visual_bg_corner_radius"
    private const val KEY_VISUAL_SET_DARK_MODE_COLOR = "visual_set_dark_mode_color"
    private const val KEY_VISUAL_BG_COLOR = "visual_bg_color"
    private const val KEY_VISUAL_BG_COLOR_DARK = "visual_bg_color_dark"
    private const val KEY_VISUAL_APP_NAME_COLOR = "visual_app_name_color"
    private const val KEY_VISUAL_APP_NAME_COLOR_DARK = "visual_app_name_color_dark"
    private const val KEY_LAUNCHER_PKG_NAME = "general_set_launcher_package_name"
    // end Old keys

    // Upgrade old SharedPreferences data to new data
    fun upgradeFromSharedPrefAndSave() {
        val defaultPref = PreferenceManager.getDefaultSharedPreferences(FrequawApp.appContext)
        val frequawPref = FrequawApp.appContext.getSharedPreferences(SharedPref.PREF_NAME, Context.MODE_PRIVATE)

        if (frequawPref.all.isEmpty()) {
            // No sharedPref data, skip
            return
        }

        if (frequawPref.all[FrequawDataHelper.PREF_KEY] is String) {
            // This sharedPref data doesn't need to be upgraded
            return
        }

        // upgrade and create FrequawData
        val newData = FrequawData.default()

        newData.appInfos.addAll(
            (frequawPref.getStringSet(KEY_APP_INFO_LIST, emptySet()) ?: emptySet())
                .map {
                    val split = it.split('|')
                    var pkgName = ""
                    var lastLaunched = 0L
                    val launchedCnt = MutableList<Long>(AppInfo.launchedCountsBy30mSize) { 0 }
                    when (split.size) {
                        3 -> {
                            pkgName = split[2]
                            lastLaunched = split[1].toLong()
                            launchedCnt.fill((split[0].toLong() / AppInfo.launchedCountsBy30mSize.toFloat()).roundToLong())
                        }
                        4 -> {
                            pkgName = split[3]
                            lastLaunched = split[2].toLong()
                            split[1].split(',').forEachIndexed { index, s ->
                                launchedCnt[index] = s.toLong()
                            }
                        }
                        else -> {
                            pkgName = split[0]

                        }
                    }
                    FrequawAppInfoData(pkgName, lastLaunched, launchedCnt.toList())
                }.filterNot { it.packageName.isEmpty() }
        )
        newData.isProMode = frequawPref.getBoolean(KEY_IS_PRO_MODE, false)
        newData.homeLauncherPackageName = defaultPref.getString(KEY_LAUNCHER_PKG_NAME, "") ?: ""

        val defaultWidgetSetting = newData.widgetSettings[AppWidgetManager.INVALID_APPWIDGET_ID]!!

        // Sort app by
        defaultWidgetSetting.sortAppBy =
            when (frequawPref.getString(KEY_APP_DETECT_MODE, "unknown")) {
                "accessibility" -> SortMode.Count
                "usage" -> SortMode.Period
                "recommend" -> SortMode.Recommend
                "recent" -> SortMode.Recent
                else -> SortMode.Recommend
            }

        // Filter mode
        defaultWidgetSetting.filterMode =
            when (frequawPref.getBoolean(KEY_APP_FILTER_MODE_IS_BLOCK, true)) {
                true -> FilterMode.BlockList
                false -> FilterMode.AllowList
            }

        // Blocked list
        defaultWidgetSetting.blockApps =
            (frequawPref.getStringSet(KEY_APP_BLOCK_FILTER_LIST, null) ?: listOf<String>())
                .toMutableSet()

        // Allowed list
        defaultWidgetSetting.allowApps =
            (frequawPref.getStringSet(KEY_APP_ALLOW_FILTER_LIST, null) ?: listOf<String>())
                .toMutableSet()

        // Sorting direction
        defaultWidgetSetting.sortingDirection =
            when (defaultPref.getString(KEY_SORTING_DIRECTION, "top_left")) {
                "top_left" -> SortingDirection.LeftTop
                "top_right" -> SortingDirection.RightTop
                "bottom_left" -> SortingDirection.LeftBottom
                "bottom_right" -> SortingDirection.RightBottom
                else -> SortingDirection.LeftTop
            }

        // Pinned app list
        defaultWidgetSetting.pinnedApps =
            (frequawPref.getStringSet(KEY_PIN_APP_FIRST_LIST, null) ?: listOf<String>())
                .toMutableSet()

        // Title visible
        defaultWidgetSetting.isTitleVisible =
            defaultPref.getBoolean(KEY_IS_TITLE_VISIBLE, true)

        // Title update time visible
        defaultWidgetSetting.isTitleUpdateTimeVisible =
            defaultPref.getBoolean(KEY_IS_TITLE_UPDATE_TIME_VISIBLE, true)

        // Title text
        defaultWidgetSetting.titleText =
            defaultPref.getString(KEY_TITLE_TEXT, "Frequaw") ?: "Frequaw"

        // Icon style
        defaultWidgetSetting.appIconStyle =
            when (frequawPref.getInt(KEY_ICON_STYLE, 0)) {
                0 -> AppIconStyle.System
                1 -> AppIconStyle.Square
                2 -> AppIconStyle.Squircle
                3 -> AppIconStyle.Circle
                4 -> AppIconStyle.RoundedSquare
                else -> AppIconStyle.System
            }

        // Icon pack
        defaultWidgetSetting.appIconPackPackage =
            frequawPref.getString(KEY_ICON_PACK_PKG, "") ?: ""

        // Icon force clip
        defaultWidgetSetting.isForceIconShapeClip =
            frequawPref.getBoolean(KEY_ICON_SHAPE_FORCE, false)

        // Icon size
        defaultWidgetSetting.appIconSize =
            defaultPref.getInt(KEY_VISUAL_ICON_SIZE, 45)

        // Is Advanced widget layout
        defaultWidgetSetting.isAdvancedWidgetLayout =
            defaultPref.getBoolean(KEY_IS_VISUAL_ADVANCED_ENABLED, false)

        // Adv Is Horizontal Vertical Icon Gap separate
        defaultWidgetSetting.advLytIsHorVerIconGapSeparate =
            defaultPref.getBoolean(KEY_IS_VISUAL_ICON_GAP_SET_SEPARATE, false)

        // Adv Horizontal Icon Gap
        defaultWidgetSetting.advLytHorIconGapSize =
            defaultPref.getInt(KEY_VISUAL_ICON_GAP_MARGIN, 3)

        // Adv Vertical Icon Gap
        defaultWidgetSetting.advLytVerIconGapSize =
            defaultPref.getInt(KEY_VISUAL_ICON_GAP_VERTICAL_MARGIN, 3)

        // Adv Is Horizontal Vertical List padding separate
        defaultWidgetSetting.advLytIsHorVerListPaddingSeparate =
            defaultPref.getBoolean(KEY_IS_VISUAL_LIST_MARGIN_SET_SEPARATE, false)

        // Adv Horizontal List padding
        defaultWidgetSetting.advLytHorListPaddingSize =
            defaultPref.getInt(KEY_VISUAL_LIST_MARGIN, 3)

        // Adv Vertical List padding
        defaultWidgetSetting.advLytVerListPaddingSize =
            defaultPref.getInt(KEY_VISUAL_LIST_VERTICAL_MARGIN, 3)

        // Is Show App Name
        defaultWidgetSetting.isShowAppsName =
            defaultPref.getBoolean(KEY_IS_VISUAL_SHOW_APP_NAME, true)

        // App name size
        defaultWidgetSetting.appsNameSize =
            defaultPref.getInt(KEY_VISUAL_APP_NAME_TEXT_SIZE, 12)

        // App name margin
        defaultWidgetSetting.appsNameMargin =
            defaultPref.getInt(KEY_VISUAL_APP_NAME_MARGIN, 3)

        // Background corner radius
        defaultWidgetSetting.backgroundCornerRadius =
            defaultPref.getInt(KEY_VISUAL_BG_CORNER_RADIUS, 11)

        // Is Set DarkMode color
        defaultWidgetSetting.isSetDarkModeColor =
            defaultPref.getBoolean(KEY_VISUAL_SET_DARK_MODE_COLOR, false)

        // Background Color
        defaultWidgetSetting.backgroundColor =
            defaultPref.getInt(KEY_VISUAL_BG_COLOR, Color.BLACK)

        // Background DarkMode Color
        defaultWidgetSetting.backgroundDarkModeColor =
            defaultPref.getInt(KEY_VISUAL_BG_COLOR_DARK, Color.WHITE)

        // Text Color
        defaultWidgetSetting.textColor =
            defaultPref.getInt(KEY_VISUAL_APP_NAME_COLOR, Color.WHITE)

        // Text DarkMode Color
        defaultWidgetSetting.textDarkModeColor =
            defaultPref.getInt(KEY_VISUAL_APP_NAME_COLOR_DARK, Color.BLACK)

        // clear old one
        defaultPref.edit().clear().apply()
        frequawPref.edit().clear().apply()

        // save
        FrequawDataHelper.save(newData)
    }

    // Upgrade old SharedPref-based json which doesn't have the "appVersionCode" field to the new one.
    fun upgradeFromOldJson(json: String): FrequawData {
        val oldData = Gson().fromJson(json, FrequawOldJsonData::class.java)

        if (oldData.dataVersion > 0) {
            // This json doesn't need to be upgraded
            return FrequawDataHelper.gsonFromJsonAndFitVersion(json)
        }

        val newData = FrequawData.default()
        val widgetSetting = newData.widgetSettings[AppWidgetManager.INVALID_APPWIDGET_ID]!!

        widgetSetting.sortAppBy = when (oldData.sortAppBy) {
            "accessibility" -> SortMode.Count
            "usage" -> SortMode.Period
            "recommend" -> SortMode.Recommend
            "recent" -> SortMode.Recent
            else -> SortMode.Recommend
        }
        widgetSetting.filterMode = when (oldData.isFilterModeBlock) {
            true -> FilterMode.BlockList
            false -> FilterMode.AllowList
        }
        widgetSetting.blockApps = oldData.blockApps.toMutableSet()
        widgetSetting.allowApps = oldData.allowApps.toMutableSet()
        widgetSetting.sortingDirection = when (oldData.sortingDirection) {
            "top_left" -> SortingDirection.LeftTop
            "top_right" -> SortingDirection.RightTop
            "bottom_left" -> SortingDirection.LeftBottom
            "bottom_right" -> SortingDirection.RightBottom
            else -> SortingDirection.LeftTop
        }
        widgetSetting.pinnedApps = oldData.pinnedApps.toMutableSet()

        widgetSetting.isTitleVisible = oldData.isTitleVisible
        widgetSetting.isTitleUpdateTimeVisible = oldData.isTitleUpdateTimeVisible
        widgetSetting.titleText = oldData.titleText
        widgetSetting.appIconStyle = when (oldData.appIconStyle) {
            0 -> AppIconStyle.System
            1 -> AppIconStyle.Square
            2 -> AppIconStyle.Squircle
            3 -> AppIconStyle.Circle
            4 -> AppIconStyle.RoundedSquare
            else -> AppIconStyle.System
        }
        widgetSetting.appIconPackPackage = oldData.appIconPackPackage
        widgetSetting.isForceIconShapeClip = oldData.isForceIconShapeClip
        widgetSetting.appIconSize = oldData.appIconSize

        widgetSetting.isAdvancedWidgetLayout = oldData.isAdvancedWidgetLayout
        widgetSetting.advLytIsHorVerIconGapSeparate = oldData.advLytIsHorVerIconGapSeparate
        widgetSetting.advLytHorIconGapSize = oldData.advLytHorIconGapSize
        widgetSetting.advLytVerIconGapSize = oldData.advLytVerIconGapSize
        widgetSetting.advLytIsHorVerListPaddingSeparate = oldData.advLytIsHorVerListPaddingSeparate
        widgetSetting.advLytHorListPaddingSize = oldData.advLytHorListPaddingSize
        widgetSetting.advLytVerListPaddingSize = oldData.advLytVerListPaddingSize

        widgetSetting.isShowAppsName = oldData.isShowAppsName
        widgetSetting.appsNameSize = oldData.appsNameSize
        widgetSetting.appsNameMargin = oldData.appsNameMargin
        widgetSetting.backgroundCornerRadius = oldData.backgroundCornerRadius * 2
        widgetSetting.isSetDarkModeColor = oldData.isSetDarkModeColor
        widgetSetting.backgroundColor = oldData.backgroundColor
        widgetSetting.backgroundDarkModeColor = oldData.backgroundDarkModeColor
        widgetSetting.textColor = oldData.textColor
        widgetSetting.textDarkModeColor = oldData.textDarkModeColor

        newData.homeLauncherPackageName = oldData.homeLaucherPackageName
        newData.isProMode = oldData.isProMode
        newData.appInfos.addAll(oldData.appInfos
            .map {
                val split = it.split('|')
                var pkgName = ""
                var lastLaunched = 0L
                val launchedCnt = MutableList<Long>(AppInfo.launchedCountsBy30mSize) { 0 }
                when (split.size) {
                    3 -> {
                        pkgName = split[2]
                        lastLaunched = split[1].toLong()
                        launchedCnt.fill((split[0].toLong() / AppInfo.launchedCountsBy30mSize.toFloat()).roundToLong())
                    }
                    4 -> {
                        pkgName = split[3]
                        lastLaunched = split[2].toLong()
                        split[1].split(',').forEachIndexed { index, s ->
                            launchedCnt[index] = s.toLong()
                        }
                    }
                    else -> {
                        pkgName = split[0]

                    }
                }
                FrequawAppInfoData(pkgName, lastLaunched, launchedCnt.toList())
            }.filterNot { it.packageName.isEmpty() }
        )

        return newData
    }

    fun restoreFromV1ObfuscatedData(json: String) : FrequawData {
        val v1ObfData = Gson().fromJson(json, com.jujinkim.frequaw.datav1.FrequawV1ObfuscatedData::class.java)
        if (v1ObfData.appInfos == null) return FrequawData.default()

        val data = FrequawData(
            _isProMode = v1ObfData.isProMode,
            _homeLauncherPackageName = v1ObfData.homeLauncherPackageName,
        ).apply {
            val newAppInfos = v1ObfData.appInfos.map {
                FrequawAppInfoData(
                    _packageName = it.packageName,
                    _lastLaunched = it.lastLaunched,
                    _launchedCount = it.launchedCount
                )
            }
            appInfos.clear()
            appInfos.addAll(newAppInfos)

            val newWidgetSettings = v1ObfData.widgetSettings.map {
                val oldWidgetId = it.key
                val oldWidgetSettingData = it.value

                val newWidgetSettingData = FrequawWidgetSettingData(
                    _widgetId = oldWidgetId,
                    _sortAppBy = oldWidgetSettingData.sortAppBy,
                    _recommendSortMode = RcmdSortMode.Balanced,
                    _filterMode = oldWidgetSettingData.filterMode,
                    _blockApps = oldWidgetSettingData.blockApps.toMutableSet(),
                    _allowApps = oldWidgetSettingData.allowApps.toMutableSet(),
                    _sortingDirection = oldWidgetSettingData.sortingDirection,
                    _pinnedApps = oldWidgetSettingData.pinnedApps.toMutableSet(),

                    _isTitleVisible = oldWidgetSettingData.isTitleVisible,
                    _isTitleUpdateTimeVisible = oldWidgetSettingData.isTitleUpdateTimeVisible,
                    _titleText = oldWidgetSettingData.titleText,
                    _appIconStyle = oldWidgetSettingData.appIconStyle,
                    _appIconPackPackage = oldWidgetSettingData.appIconPackPackage,
                    _isForceIconShapeClip = oldWidgetSettingData.isForceIconShapeClip,
                    _appIconSize = oldWidgetSettingData.appIconSize,

                    _isAdvancedWidgetLayout = oldWidgetSettingData.isAdvancedWidgetLayout,
                    _advLytIsHorVerIconGapSeparate = oldWidgetSettingData.advLytIsHorVerIconGapSeparate,
                    _advLytHorIconGapSize = oldWidgetSettingData.advLytHorIconGapSize,
                    _advLytVerIconGapSize = oldWidgetSettingData.advLytVerIconGapSize,
                    _advLytIsHorVerListPaddingSeparate = oldWidgetSettingData.advLytIsHorVerListPaddingSeparate,
                    _advLytHorListPaddingSize = oldWidgetSettingData.advLytHorListPaddingSize,
                    _advLytVerListPaddingSize = oldWidgetSettingData.advLytVerListPaddingSize,

                    _isShowAppsName = oldWidgetSettingData.isShowAppsName,
                    _appsNameSize = oldWidgetSettingData.appsNameSize,
                    _appsNameMargin = oldWidgetSettingData.appsNameMargin,
                    _backgroundCornerRadius = oldWidgetSettingData.backgroundCornerRadius,
                    _isSetDarkModeColor = oldWidgetSettingData.isSetDarkModeColor,
                    _backgroundColor = oldWidgetSettingData.backgroundColor,
                    _backgroundDarkModeColor = oldWidgetSettingData.backgroundDarkModeColor,
                    _textColor = oldWidgetSettingData.textColor,
                    _textDarkModeColor = oldWidgetSettingData.textDarkModeColor
                )

                oldWidgetId to newWidgetSettingData
            }
            widgetSettings.clear()
            widgetSettings.putAll(newWidgetSettings)
        }

        return data
    }

    class FrequawOldJsonData(
        @SerializedName("dataVersion") val dataVersion: Int = 0,

        @SerializedName("f$KEY_APP_DETECT_MODE") val sortAppBy: String = "recommend",
        @SerializedName("f$KEY_APP_FILTER_MODE_IS_BLOCK") val isFilterModeBlock: Boolean = true,
        @SerializedName("f$KEY_APP_BLOCK_FILTER_LIST") val blockApps: MutableList<String> = mutableListOf(),
        @SerializedName("f$KEY_APP_ALLOW_FILTER_LIST") val allowApps: MutableList<String> = mutableListOf(),
        @SerializedName("d$KEY_SORTING_DIRECTION") val sortingDirection: String = "top_left",
        @SerializedName("f$KEY_PIN_APP_FIRST_LIST") val pinnedApps: MutableList<String> = mutableListOf(),

        @SerializedName("d$KEY_IS_TITLE_VISIBLE") val isTitleVisible: Boolean = true,
        @SerializedName("d$KEY_IS_TITLE_UPDATE_TIME_VISIBLE") val isTitleUpdateTimeVisible: Boolean = true,
        @SerializedName("d$KEY_TITLE_TEXT") val titleText: String = "Frequaw",
        @SerializedName("f$KEY_ICON_STYLE") val appIconStyle: Int = 0,
        @SerializedName("f$KEY_ICON_PACK_PKG") val appIconPackPackage: String = "",
        @SerializedName("f$KEY_ICON_SHAPE_FORCE") val isForceIconShapeClip: Boolean = false,
        @SerializedName("d$KEY_VISUAL_ICON_SIZE") val appIconSize: Int = 40,

        @SerializedName("d$KEY_IS_VISUAL_ADVANCED_ENABLED") val isAdvancedWidgetLayout: Boolean = false,
        @SerializedName("d$KEY_IS_VISUAL_ICON_GAP_SET_SEPARATE") val advLytIsHorVerIconGapSeparate: Boolean = false,
        @SerializedName("d$KEY_VISUAL_ICON_GAP_MARGIN") val advLytHorIconGapSize: Int = 5,
        @SerializedName("d$KEY_VISUAL_ICON_GAP_VERTICAL_MARGIN") val advLytVerIconGapSize: Int = 5,
        @SerializedName("d$KEY_IS_VISUAL_LIST_MARGIN_SET_SEPARATE") val advLytIsHorVerListPaddingSeparate: Boolean = false,
        @SerializedName("d$KEY_VISUAL_LIST_MARGIN") val advLytHorListPaddingSize: Int = 5,
        @SerializedName("d$KEY_VISUAL_LIST_VERTICAL_MARGIN") val advLytVerListPaddingSize: Int = 5,

        @SerializedName("d$KEY_IS_VISUAL_SHOW_APP_NAME") val isShowAppsName: Boolean = false,
        @SerializedName("d$KEY_VISUAL_APP_NAME_TEXT_SIZE") val appsNameSize: Int = 12,
        @SerializedName("d$KEY_VISUAL_APP_NAME_MARGIN") val appsNameMargin: Int = 5,
        @SerializedName("d$KEY_VISUAL_BG_CORNER_RADIUS") val backgroundCornerRadius: Int = 22,
        @SerializedName("d$KEY_VISUAL_SET_DARK_MODE_COLOR") val isSetDarkModeColor: Boolean = false,
        @SerializedName("d$KEY_VISUAL_BG_COLOR") val backgroundColor: Int = Color.WHITE,
        @SerializedName("d$KEY_VISUAL_BG_COLOR_DARK") val backgroundDarkModeColor: Int = Color.BLACK,
        @SerializedName("d$KEY_VISUAL_APP_NAME_COLOR") val textColor: Int = Color.BLACK,
        @SerializedName("d$KEY_VISUAL_APP_NAME_COLOR_DARK") val textDarkModeColor: Int = Color.WHITE,

        @SerializedName("f$KEY_IS_PRO_MODE") val isProMode: Boolean = false,
        @SerializedName("d$KEY_LAUNCHER_PKG_NAME") val homeLaucherPackageName: String = "",
        @SerializedName("f$KEY_APP_INFO_LIST") val appInfos: MutableList<String> = mutableListOf()
    )
}


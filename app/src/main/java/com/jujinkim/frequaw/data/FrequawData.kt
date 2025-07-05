package com.jujinkim.frequaw.data

import android.appwidget.AppWidgetManager
import android.graphics.Color
import com.google.gson.annotations.SerializedName
import com.jujinkim.frequaw.*
import kotlin.math.roundToLong

data class FrequawData(
    @SerializedName(value = "appInfos") val _appInfos: MutableList<FrequawAppInfoData>? = mutableListOf(),
    @SerializedName(value = "widgetSettings") val _widgetSettings: HashMap<Int, FrequawWidgetSettingData>? = HashMap(),
    @SerializedName(value = "isProMode") var _isProMode: Boolean?,
    @SerializedName(value = "homeLauncherPackageName") var _homeLauncherPackageName: String?
) {
    @SerializedName(value = "dataVersion") var dataVersion = 2
    private set

    val appInfos: MutableList<FrequawAppInfoData>
        get() = _appInfos ?: mutableListOf()
    val widgetSettings: HashMap<Int, FrequawWidgetSettingData>
        get() = _widgetSettings ?: HashMap()
    var isProMode: Boolean
        get() = _isProMode ?: false
        set(value) {
            _isProMode = value
        }
    var homeLauncherPackageName: String
        get() = _homeLauncherPackageName ?: ""
        set(value) {
            _homeLauncherPackageName = value
        }

    fun getWidgetSetting(widgetId: Int): FrequawWidgetSettingData {
        return widgetSettings.getOrElse(widgetId) {
            widgetSettings.getOrPut(AppWidgetManager.INVALID_APPWIDGET_ID) {
                FrequawWidgetSettingData.default(AppWidgetManager.INVALID_APPWIDGET_ID)
            }
        }
    }

    fun reduceLaunchedCounts() {
        appInfos.forEach { info ->
            info.launchedCount = info.launchedCount.map {
                if (it > 6) {
                    (it * 2 / 3f).roundToLong().coerceAtLeast(5)
                } else {
                    (it - 1).coerceAtLeast(0)
                }
            }
        }
    }

    companion object {
        fun default() = FrequawData(
            _appInfos = mutableListOf(),
            _widgetSettings = HashMap(),
            _isProMode = false,
            _homeLauncherPackageName = ""
        ).apply {
            widgetSettings.set(AppWidgetManager.INVALID_APPWIDGET_ID,
                FrequawWidgetSettingData.default(AppWidgetManager.INVALID_APPWIDGET_ID)
            )
        }
    }

    /** Set the version of this Frequaw Data. DO NOT CALL THIS WITHOUT REASON!*/
    fun setVersion(version: Int) {
        dataVersion = version
    }
}

data class FrequawWidgetSettingData(
    @SerializedName(value = "widgetId") val _widgetId: Int?,
    @SerializedName(value = "sortAppBy") var _sortAppBy: SortMode?,
    @SerializedName(value = "recommendSortMode") var _recommendSortMode: RcmdSortMode?,    // version 2
    @SerializedName(value = "filterMode") var _filterMode: FilterMode?,
    @SerializedName(value = "blockApps") var _blockApps: MutableSet<String>?,
    @SerializedName(value = "allowApps") var _allowApps: MutableSet<String>?,
    @SerializedName(value = "sortingDirection") var _sortingDirection: SortingDirection?,
    @SerializedName(value = "pinnedApps") var _pinnedApps: MutableSet<String>?,

    @SerializedName(value = "isTitleVisible") var _isTitleVisible: Boolean?,
    @SerializedName(value = "isTitleUpdateTimeVisible") var _isTitleUpdateTimeVisible: Boolean?,
    @SerializedName(value = "titleText") var _titleText: String?,
    @SerializedName(value = "appIconStyle") var _appIconStyle: AppIconStyle?,
    @SerializedName(value = "appIconPackPackage") var _appIconPackPackage: String?,
    @SerializedName(value = "isForceIconShapeClip") var _isForceIconShapeClip: Boolean?,
    @SerializedName(value = "appIconSize") var _appIconSize: Int?,

    @SerializedName(value = "isAdvancedWidgetLayout") var _isAdvancedWidgetLayout: Boolean?,
    @SerializedName(value = "advLytIsHorVerIconGapSeparate") var _advLytIsHorVerIconGapSeparate: Boolean?,
    @SerializedName(value = "advLytHorIconGapSize") var _advLytHorIconGapSize: Int?,
    @SerializedName(value = "advLytVerIconGapSize") var _advLytVerIconGapSize: Int?,
    @SerializedName(value = "advLytIsHorVerListPaddingSeparate") var _advLytIsHorVerListPaddingSeparate: Boolean?,
    @SerializedName(value = "advLytHorListPaddingSize") var _advLytHorListPaddingSize: Int?,
    @SerializedName(value = "advLytVerListPaddingSize") var _advLytVerListPaddingSize: Int?,

    @SerializedName(value = "isShowAppsName") var _isShowAppsName: Boolean?,
    @SerializedName(value = "appsNameSize") var _appsNameSize: Int?,
    @SerializedName(value = "appsNameMargin") var _appsNameMargin: Int?,
    @SerializedName(value = "backgroundCornerRadius") var _backgroundCornerRadius: Int?,
    @SerializedName(value = "isSetDarkModeColor") var _isSetDarkModeColor: Boolean?,
    @SerializedName(value = "backgroundColor") var _backgroundColor: Int?,
    @SerializedName(value = "backgroundDarkModeColor") var _backgroundDarkModeColor: Int?,
    @SerializedName(value = "textColor") var _textColor: Int?,
    @SerializedName(value = "textDarkModeColor") var _textDarkModeColor: Int?,
) {

    val widgetId: Int
        get() = _widgetId ?: AppWidgetManager.INVALID_APPWIDGET_ID
    var sortAppBy: SortMode
        get() = _sortAppBy ?: SortMode.Count
        set(value) {
            _sortAppBy = value
        }
    var recommendSortMode: RcmdSortMode
        get() = _recommendSortMode ?: RcmdSortMode.Balanced
        set(value) {
            _recommendSortMode = value
        }
    var filterMode: FilterMode
        get() = _filterMode ?: FilterMode.BlockList
        set(value) {
            _filterMode = value
        }
    var blockApps: MutableSet<String>
        get() = _blockApps ?: mutableSetOf()
        set(value) {
            _blockApps = value
        }
    var allowApps: MutableSet<String>
        get() = _allowApps ?: mutableSetOf()
        set(value) {
            _allowApps = value
        }
    var sortingDirection: SortingDirection
        get() = _sortingDirection ?: SortingDirection.LeftTop
        set(value) {
            _sortingDirection = value
        }
    var pinnedApps: MutableSet<String>
        get() = _pinnedApps ?: mutableSetOf()
        set(value) {
            _pinnedApps = value
        }

    var isTitleVisible: Boolean
        get() = _isTitleVisible ?: true
        set(value) {
            _isTitleVisible = value
        }
    var isTitleUpdateTimeVisible: Boolean
        get() = _isTitleUpdateTimeVisible ?: true
        set(value) {
            _isTitleUpdateTimeVisible = value
        }
    var titleText: String
        get() = _titleText ?: "Frequaw"
        set(value) {
            _titleText = value
        }
    var appIconStyle: AppIconStyle
        get() = _appIconStyle ?: AppIconStyle.System
        set(value) {
            _appIconStyle = value
        }
    var appIconPackPackage: String
        get() = _appIconPackPackage ?: ""
        set(value) {
            _appIconPackPackage = value
        }
    var isForceIconShapeClip: Boolean
        get() = _isForceIconShapeClip ?: false
        set(value) {
            _isForceIconShapeClip = value
        }
    var appIconSize: Int
        get() = _appIconSize ?: 45
        set(value) {
            _appIconSize = value
        }

    var isAdvancedWidgetLayout: Boolean
        get() = _isAdvancedWidgetLayout ?: false
        set(value) {
            _isAdvancedWidgetLayout = value
        }
    var advLytIsHorVerIconGapSeparate: Boolean
        get() = _advLytIsHorVerIconGapSeparate ?: false
        set(value) {
            _advLytIsHorVerIconGapSeparate = value
        }
    var advLytHorIconGapSize: Int
        get() = _advLytHorIconGapSize ?: 3
        set(value) {
            _advLytHorIconGapSize = value
        }
    var advLytVerIconGapSize: Int
        get() = _advLytVerIconGapSize ?: 3
        set(value) {
            _advLytVerIconGapSize = value
        }
    var advLytIsHorVerListPaddingSeparate: Boolean
        get() = _advLytIsHorVerListPaddingSeparate ?: false
        set(value) {
            _advLytIsHorVerListPaddingSeparate = value
        }
    var advLytHorListPaddingSize: Int
        get() = _advLytHorListPaddingSize ?: 3
        set(value) {
            _advLytHorListPaddingSize = value
        }
    var advLytVerListPaddingSize: Int
        get() = _advLytVerListPaddingSize ?: 3
        set(value) {
            _advLytVerListPaddingSize = value
        }

    var isShowAppsName: Boolean
        get() = _isShowAppsName ?: true
        set(value) {
            _isShowAppsName = value
        }
    var appsNameSize: Int
        get() = _appsNameSize ?: 12
        set(value) {
            _appsNameSize = value
        }
    var appsNameMargin: Int
        get() = _appsNameMargin ?: 3
        set(value) {
            _appsNameMargin = value
        }
    var backgroundCornerRadius: Int
        get() = _backgroundCornerRadius ?: 11
        set(value) {
            _backgroundCornerRadius = value
        }
    var isSetDarkModeColor: Boolean
        get() = _isSetDarkModeColor ?: false
        set(value) {
            _isSetDarkModeColor = value
        }
    var backgroundColor: Int
        get() = _backgroundColor ?: Color.WHITE
        set(value) {
            _backgroundColor = value
        }
    var backgroundDarkModeColor: Int
        get() = _backgroundDarkModeColor ?: Color.BLACK
        set(value) {
            _backgroundDarkModeColor = value
        }
    var textColor: Int
        get() = _textColor ?: Color.BLACK
        set(value) {
            _textColor = value
        }
    var textDarkModeColor: Int
        get() = _textDarkModeColor ?: Color.WHITE
        set(value) {
            _textDarkModeColor = value
        }

    companion object {
        fun default(widgetId: Int) = FrequawWidgetSettingData(
            _widgetId = widgetId,
            _sortAppBy = SortMode.Count,
            _recommendSortMode = RcmdSortMode.Balanced,
            _filterMode = FilterMode.BlockList,
            _blockApps = mutableSetOf(),
            _allowApps = mutableSetOf(),
            _sortingDirection = SortingDirection.LeftTop,
            _pinnedApps = mutableSetOf(),

            _isTitleVisible = true,
            _isTitleUpdateTimeVisible = true,
            _titleText = "Frequaw",
            _appIconStyle = AppIconStyle.System,
            _appIconPackPackage = "",
            _isForceIconShapeClip = false,
            _appIconSize = 45,

            _isAdvancedWidgetLayout = false,
            _advLytIsHorVerIconGapSeparate = false,
            _advLytHorIconGapSize = 3,
            _advLytVerIconGapSize = 3,
            _advLytIsHorVerListPaddingSeparate = false,
            _advLytHorListPaddingSize = 3,
            _advLytVerListPaddingSize = 3,

            _isShowAppsName = false,
            _appsNameSize = 12,
            _appsNameMargin = 5,
            _backgroundCornerRadius = 11,
            _isSetDarkModeColor = false,
            _backgroundColor = Color.BLACK,
            _backgroundDarkModeColor = Color.WHITE,
            _textColor = Color.WHITE,
            _textDarkModeColor = Color.BLACK,
        )
    }
}

data class FrequawAppInfoData(
    @SerializedName(value = "packageName") val _packageName: String?,
    @SerializedName(value = "lastLaunched") var _lastLaunched: Long?,
    @SerializedName(value = "launchedCount") var _launchedCount: List<Long>?
) {
    val packageName: String
        get() = _packageName ?: ""
    var lastLaunched: Long
        get() = _lastLaunched ?: 0
        set(value) {
            _lastLaunched = value
        }
    var launchedCount: List<Long>
        get() = _launchedCount ?: listOf(0)
        set(value) {
            _launchedCount = value
        }

    companion object {
        fun default(packageName: String) = FrequawAppInfoData(
            _packageName = packageName,
            _lastLaunched = 0,
            _launchedCount = listOf(0)
        )
    }
}
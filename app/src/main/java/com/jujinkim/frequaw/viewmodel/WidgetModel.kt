package com.jujinkim.frequaw.viewmodel

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.view.View
import com.jujinkim.frequaw.*
import com.jujinkim.frequaw.Utils.dp2px
import com.jujinkim.frequaw.Utils.sp2px
import com.jujinkim.frequaw.applist.AppListManager
import com.jujinkim.frequaw.data.FrequawData
import com.jujinkim.frequaw.data.FrequawDataHelper
import com.jujinkim.frequaw.data.FrequawWidgetSettingData
import com.jujinkim.frequaw.model.AppInfo

class WidgetModel(val widgetId: Int) {

    // title
    var titleVisibility = View.VISIBLE
    var updateTimeVisibility = View.VISIBLE
    var updateTime = "--"
    var titleText = ""

    // style
    var bgColor = Color.BLACK
    var bgAlpha = 255
    var bgRes = R.drawable.widget_rounded_bg_22dp
    var badgeBg = Color.WHITE
    var badgeTextColor = Color.BLACK
    var isBadgeShow = true
    var isBadgeDotOnly = true
    var isAdvancedEnabled = false
    var isNameShow = false
    var textColor = Color.WHITE
    var widgetHorizontalPadding = 0
    var widgetVerticalPadding = 0

    // App icons
    var iconSize = 0
    var iconHorizontalGap = 0
    var iconVerticalGap = 0
    var appList : List<AppInfo> = listOf()
    var appNameTopMargin = 0
    var appNameSpSize = 0f
    var appNamePxSize = 0
    var iconAreaWidth = 0
    var iconAreaHeight = 0

    var appSortDirection = SortingDirection.LeftTop

    fun initializeData(context: Context) {
        val settingData = FrequawDataHelper.load()
        val widgetData = FrequawDataHelper.loadWidgetSetting(widgetId)

        // title
        titleVisibility =
            if (widgetData.isTitleVisible)
                View.VISIBLE
            else
                View.GONE

        updateTimeVisibility =
            if (widgetData.isTitleUpdateTimeVisible)
                View.VISIBLE
            else
                View.GONE
        updateTime =
            if (SharedPref.getDebugMode()) debugString(settingData, widgetData)
            else AppListManager.lastUpdateTime
        titleText = widgetData.titleText

        // style
//        isBadgeShow =
//            pref.getBoolean(getKeyWithId(context, "badge_enable_badge"), true)
//        isBadgeDotOnly =
//            !pref.getBoolean(getKeyWithId(context, "badge_show_num"), true)

        // icon
        iconSize = widgetData.appIconSize.dp2px()
        isNameShow = widgetData.isShowAppsName
        appNameTopMargin = widgetData.appsNameMargin.dp2px()
        appNameSpSize = widgetData.appsNameSize.toFloat()
        appNamePxSize = appNameSpSize.sp2px()

        iconAreaWidth = iconSize
        iconAreaHeight =
            if (isNameShow)
                iconSize + appNameTopMargin + appNamePxSize
            else
                iconSize

        isAdvancedEnabled = widgetData.isAdvancedWidgetLayout
        if (!isAdvancedEnabled) {
            val factor = 0.07   // I think this value is the best value in my eyes..
            widgetHorizontalPadding = (iconSize * factor).toInt()
            widgetVerticalPadding = (iconSize * factor).toInt()
            iconHorizontalGap = (iconSize * factor).toInt()
            iconVerticalGap = (iconSize * factor).toInt()
        } else {
            // icon margin
            val isSeparateIconMargin = widgetData.advLytIsHorVerIconGapSeparate
            iconHorizontalGap = widgetData.advLytHorIconGapSize.dp2px()
            iconVerticalGap =
                if (isSeparateIconMargin) widgetData.advLytVerIconGapSize.dp2px()
                else iconHorizontalGap

            // widget padding
            val isSeparateWidgetPadding = widgetData.advLytIsHorVerListPaddingSeparate
            widgetHorizontalPadding = widgetData.advLytHorListPaddingSize.dp2px()
            widgetVerticalPadding =
                if (isSeparateWidgetPadding) widgetData.advLytVerListPaddingSize.dp2px()
                else widgetHorizontalPadding
        }

        // color
        val isDarkModeOverride = widgetData.isSetDarkModeColor
        val darkMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (darkMode == Configuration.UI_MODE_NIGHT_YES && isDarkModeOverride) {
            val bgArgbColor = widgetData.backgroundDarkModeColor
            bgColor = Color.rgb(Color.red(bgArgbColor), Color.green(bgArgbColor), Color.blue(bgArgbColor))
            bgAlpha = Color.alpha(bgArgbColor)
            textColor =widgetData.textDarkModeColor
        } else {
            val bgArgbColor = widgetData.backgroundColor
            bgColor = Color.rgb(Color.red(bgArgbColor), Color.green(bgArgbColor), Color.blue(bgArgbColor))
            bgAlpha = Color.alpha(bgArgbColor)
            textColor = widgetData.textColor
        }

        // radius
        val bgRadius = widgetData.backgroundCornerRadius * 2
        bgRes = context.resources.getIdentifier(
            "widget_rounded_bg_${bgRadius}dp",
            "drawable",
            context.packageName)
        if (bgRes == 0) bgRes = R.drawable.widget_rounded_bg_22dp

        appSortDirection = widgetData.sortingDirection

        appList = AppListManager.getSortedApps(widgetId)
    }

    private fun debugString(data: FrequawData, widgetData: FrequawWidgetSettingData): String {
        // versionCode:
        // widgetId:
        // SortingMethod:
        // SortingDirection:
        // PinAppCount:
        // ProModeEnabled:
        // UseDefaultSetting
        return """
            ${BuildConfig.VERSION_CODE}:
            $widgetId:
            ${widgetData.sortAppBy.ordinal}:
            ${appSortDirection.ordinal}:
            ${widgetData.pinnedApps.size}:
            ${if (data.isProMode) 1 else 0}:
            ${if (widgetData.widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) 1 else 0}
        """.trimIndent().replace(" ", "").replace("\n", "")
    }
}
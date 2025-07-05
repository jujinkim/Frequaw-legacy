package com.jujinkim.frequaw.datav1

import com.google.gson.annotations.SerializedName
import com.jujinkim.frequaw.*

/*
This class is used for the dataVersion 1 old.
I'd forgot to add rule to ignore FrequawData at proguard-rules, so all of fields are obfuscated.
So, keep the original structure of FrequawData, and restore from obfuscated saved json.
 */

data class FrequawV1ObfuscatedData(
    @SerializedName("a") val appInfos: MutableList<FrequawAppInfoData> = mutableListOf(),
    @SerializedName("b") val widgetSettings: HashMap<Int, FrequawWidgetSettingData> = HashMap(),
    @SerializedName("c") var isProMode: Boolean,
    @SerializedName("d") var homeLauncherPackageName: String
) {
    val dataVersion = 1
}

data class FrequawWidgetSettingData(
    @SerializedName("a") val widgetId: Int,
    @SerializedName("b") var sortAppBy: SortMode,
    @SerializedName("c") var filterMode: FilterMode,
    @SerializedName("d") var blockApps: MutableSet<String>,
    @SerializedName("e") var allowApps: MutableSet<String>,
    @SerializedName("f") var sortingDirection: SortingDirection,
    @SerializedName("g") var pinnedApps: MutableSet<String>,

    @SerializedName("h") var isTitleVisible: Boolean,
    @SerializedName("i") var isTitleUpdateTimeVisible: Boolean,
    @SerializedName("j") var titleText: String,
    @SerializedName("k") var appIconStyle: AppIconStyle,
    @SerializedName("l") var appIconPackPackage: String,
    @SerializedName("m") var isForceIconShapeClip: Boolean,
    @SerializedName("n") var appIconSize: Int,

    @SerializedName("o") var isAdvancedWidgetLayout: Boolean,
    @SerializedName("p") var advLytIsHorVerIconGapSeparate: Boolean,
    @SerializedName("q") var advLytHorIconGapSize: Int,
    @SerializedName("r") var advLytVerIconGapSize: Int,
    @SerializedName("s") var advLytIsHorVerListPaddingSeparate: Boolean,
    @SerializedName("t") var advLytHorListPaddingSize: Int,
    @SerializedName("u") var advLytVerListPaddingSize: Int,

    @SerializedName("v") var isShowAppsName: Boolean,
    @SerializedName("w") var appsNameSize: Int,
    @SerializedName("x") var appsNameMargin: Int,
    @SerializedName("y") var backgroundCornerRadius: Int,
    @SerializedName("z") var isSetDarkModeColor: Boolean,
    @SerializedName("A") var backgroundColor: Int,
    @SerializedName("B") var backgroundDarkModeColor: Int,
    @SerializedName("C") var textColor: Int,
    @SerializedName("D") var textDarkModeColor: Int,
) {
    companion object {
        fun default(widgetId: Int) = FrequawWidgetSettingData(
            widgetId = widgetId,
            sortAppBy = SortMode.Count,
            filterMode = FilterMode.BlockList,
            blockApps = mutableSetOf(),
            allowApps = mutableSetOf(),
            sortingDirection = SortingDirection.LeftTop,
            pinnedApps = mutableSetOf(),

            isTitleVisible = true,
            isTitleUpdateTimeVisible = true,
            titleText = "Frequaw",
            appIconStyle = AppIconStyle.System,
            appIconPackPackage = "",
            isForceIconShapeClip = false,
            appIconSize = 45,

            isAdvancedWidgetLayout = false,
            advLytIsHorVerIconGapSeparate = false,
            advLytHorIconGapSize = 0,
            advLytVerIconGapSize = 0,
            advLytIsHorVerListPaddingSeparate = false,
            advLytHorListPaddingSize = 0,
            advLytVerListPaddingSize = 0,

            isShowAppsName = false,
            appsNameSize = 12,
            appsNameMargin = 5,
            backgroundCornerRadius = 0,
            isSetDarkModeColor = false,
            backgroundColor = 0,
            backgroundDarkModeColor = 0,
            textColor = 0,
            textDarkModeColor = 0,
        )
    }
}

data class FrequawAppInfoData(
    @SerializedName("a") val packageName: String,
    @SerializedName("b") var lastLaunched: Long,
    @SerializedName("c") var launchedCount: List<Long>
)
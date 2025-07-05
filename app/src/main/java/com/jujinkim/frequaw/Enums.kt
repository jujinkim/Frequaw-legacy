package com.jujinkim.frequaw

enum class SortMode { Count, Period, Recommend, Recent }
enum class RcmdSortMode { Balanced, Time, Count }
enum class FilterMode { BlockList, AllowList }
enum class SortingDirection { LeftTop, RightTop, LeftBottom, RightBottom }
enum class AppIconStyle { System, Circle, Square, RoundedSquare, Squircle }

enum class Screen {
    AppList, AppListSortMode, AppListFilterApp, AppListPinApp,
    Visual, VisualIconStyle,
    General, GeneralResetHistory,
    About
}
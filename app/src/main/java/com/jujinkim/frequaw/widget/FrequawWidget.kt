package com.jujinkim.frequaw.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.SparseIntArray
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.view.View
import android.widget.RemoteViews
import androidx.core.util.set
import com.jujinkim.frequaw.*
import com.jujinkim.frequaw.Utils.dp2px
import com.jujinkim.frequaw.data.FrequawDataHelper
import com.jujinkim.frequaw.ui.MainActivity
import com.jujinkim.frequaw.viewmodel.WidgetModel

/**
 * Implementation of App Widget functionality.
 */
class FrequawWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)

        if (context != null && appWidgetManager != null) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Wait for duplicate-prevention timer
        when (updateDuplicatePreventionFlags[appWidgetId]) {
            UPDATE_WIDGET_IDLE -> {
                updateDuplicatePreventionFlags[appWidgetId] = UPDATE_WIDGET_WAIT
                Handler(Looper.getMainLooper()).postDelayed({
                    updateDuplicatePreventionFlags[appWidgetId] = UPDATE_WIDGET_GOOD_TO_UPDATE
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }, UPDATE_WIDGET_DELAY)
                return
            }
            UPDATE_WIDGET_WAIT ->  return
            UPDATE_WIDGET_GOOD_TO_UPDATE -> updateDuplicatePreventionFlags[appWidgetId] = UPDATE_WIDGET_IDLE
            else -> updateDuplicatePreventionFlags[appWidgetId] = UPDATE_WIDGET_IDLE    // abnormal case
        }

        // Update Widget
        appWidgetManager.getAppWidgetOptions(appWidgetId)?.let { bundle ->
            updatedMinWidths[appWidgetId] = bundle.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH).dp2px()
            updatedMaxWidths[appWidgetId] = bundle.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH).dp2px()
            updatedMinHeights[appWidgetId] = bundle.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT).dp2px()
            updatedMaxHeights[appWidgetId] = bundle.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT).dp2px()
        }

        val views = RemoteViews(context.packageName, R.layout.frequaw_widget)
        val viewModel = WidgetModel(appWidgetId).apply { initializeData(context) }

        // Set progress visible
        views.setViewVisibility(R.id.progress_loading, View.VISIBLE)
        appWidgetManager.updateAppWidget(appWidgetId, views)

        // Update widget
        updateTitle(views, appWidgetId, viewModel, context)
        updateAppGrid(views, appWidgetId, viewModel, context)

        // Set progress gone
        views.setViewVisibility(R.id.progress_loading, View.GONE)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget)
        appWidgetManager.updateAppWidget(appWidgetId, views)


        PackageUtil.updateHomeLauncherPackageName(context)
    }

    private fun updateTitle(views: RemoteViews, widgetId: Int, viewModel: WidgetModel, context: Context) {
        val setting = FrequawDataHelper.loadWidgetSetting(widgetId)

        views.setViewVisibility(R.id.headerArea, viewModel.titleVisibility)
        views.setTextColor(R.id.tv_title, viewModel.textColor)
        views.setTextViewText(R.id.tv_title, viewModel.titleText)

        // updated time
        views.setViewVisibility(R.id.tv_updateTime, viewModel.updateTimeVisibility)
        views.setTextViewText(R.id.tv_updateTime, viewModel.updateTime)
        views.setTextColor(R.id.tv_updateTime, viewModel.textColor)

        // update time click event
        val updateIntent = Intent(context, FrequawWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetId)
        }

        val updatePendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            updateIntent,
            PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.tv_updateTime, updatePendingIntent)

        // check Frequaw is available or not (Accessibility service or Usage stats permission)
        val mode = setting.sortAppBy
        if (mode == SortMode.Count &&
            PermissionManager.isAccessibilityAllowed(context) &&
            PermissionManager.isServiceRunning(context)) {
            views.setViewVisibility(R.id.iv_error, View.GONE)
        } else if (mode == SortMode.Period &&
            PermissionManager.isUsageStatsAllowed(context)) {
            views.setViewVisibility(R.id.iv_error, View.GONE)
        } else if (mode == SortMode.Recommend &&
            PermissionManager.isAccessibilityAllowed(context) &&
            PermissionManager.isServiceRunning(context)) {
            views.setViewVisibility(R.id.iv_error, View.GONE)
        } else if (mode == SortMode.Recent &&
            PermissionManager.isAccessibilityAllowed(context) &&
            PermissionManager.isServiceRunning(context)) {
            views.setViewVisibility(R.id.iv_error, View.GONE)
        } else {
            views.setViewVisibility(R.id.iv_error, View.VISIBLE)
            // open setting
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.iv_error, pendingIntent)
        }

        // bg
        views.setImageViewResource(R.id.widget_bg, viewModel.bgRes)
        views.setInt(R.id.widget_bg, "setColorFilter", viewModel.bgColor)
        views.setInt(R.id.widget_bg, "setAlpha", viewModel.bgAlpha)
    }

    private fun updateAppGrid(views: RemoteViews,
                              widgetId: Int,
                              viewModel: WidgetModel,
                              context: Context) {
        val setting = FrequawDataHelper.loadWidgetSetting(widgetId)

        views.removeAllViews(R.id.appIconArea)

        val isPortrait =
            context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        // Get width/height of the current widget
        val width: Int
        val height: Int

        if (isPortrait) {
            width = updatedMinWidths[widgetId]
            height = updatedMaxHeights[widgetId]
        } else {
            width = updatedMaxWidths[widgetId]
            height = updatedMinHeights[widgetId]
        }

        // calculate column & row counts by icon size and widget's size
        val colCount =
            FrequawWidgetUtils.calcColumnCount(
                width,
                viewModel.widgetHorizontalPadding,
                viewModel.iconAreaWidth,
                viewModel.iconHorizontalGap
            )

        val rowCount =
            FrequawWidgetUtils.calcRowCount(
                height,
                viewModel.widgetVerticalPadding,
                viewModel.iconAreaHeight,
                viewModel.iconVerticalGap,
                viewModel.titleVisibility == View.GONE
            )

        // update icon gap sizes if the layout mode is not the advanced mode
        if (!viewModel.isAdvancedEnabled) {
            val titleHeight =
                if (viewModel.titleVisibility == View.GONE) 0
                else context.resources.getDimensionPixelSize(R.dimen.widget_title_height)

            viewModel.iconVerticalGap =
                ((height - titleHeight - (viewModel.iconAreaHeight * rowCount)).toFloat() / rowCount / 2).toInt()
            viewModel.iconHorizontalGap =
                ((width - (viewModel.iconAreaWidth * colCount)).toFloat() / colCount / 2).toInt()
        }

        // widget padding
        val horizontalPadding =
            FrequawWidgetUtils.calcWidgetActualHorizontalPadding(
                width,
                colCount,
                viewModel.iconAreaWidth,
                viewModel.iconHorizontalGap,
                viewModel.widgetHorizontalPadding
            )
        val verticalPadding =
            FrequawWidgetUtils.calcWidgetActualVerticalPadding(
                height,
                rowCount,
                viewModel.iconAreaHeight,
                viewModel.iconVerticalGap,
                viewModel.widgetVerticalPadding,
                viewModel.titleVisibility == View.GONE
            )

        views.setViewPadding(
            R.id.appIconArea,
            horizontalPadding,
            if (viewModel.titleVisibility == View.GONE) verticalPadding else 0,
            horizontalPadding,
            verticalPadding
        )

        // Add app icons into the layout (double linear layout)
        val appList = viewModel.appList

        //Log.d(FrequawApp.TAG_DEBUG, appList.toString())

        var rowRange: IntProgression = 0 until rowCount
        var colRange: IntProgression = 0 until colCount
        when (viewModel.appSortDirection) {
            SortingDirection.LeftTop -> { rowRange = 0 until rowCount; colRange = 0 until colCount }
            SortingDirection.RightTop -> { rowRange = 0 until rowCount; colRange = colCount - 1 downTo 0}
            SortingDirection.LeftBottom -> { rowRange = rowCount - 1 downTo 0; colRange = 0 until colCount }
            SortingDirection.RightBottom -> { rowRange = rowCount - 1 downTo 0; colRange = colCount - 1 downTo 0 }
        }

        val isHighQualityIcon = SharedPref.getUseOriginalQualityIcon()
        val curIconCountFullResolution = if (isHighQualityIcon) iconCountFullResolutionMore else iconCountFullResolution
        val curIconSizeReduceBigFactor = if (isHighQualityIcon) iconSizeReduceBigFactorMore else iconSizeReduceBigFactor
        val curIconSizeReduceFactor = if (isHighQualityIcon) iconSizeReduceFactorMore else iconSizeReduceFactor

        val iconHelper = IconHelper(context, setting.widgetId)
        val iconResizeFactor =
            if (rowCount * colCount <= curIconCountFullResolution) curIconSizeReduceBigFactor
            else curIconSizeReduceFactor

        for (y in rowRange) {
            val linearLayout = RemoteViews(context.packageName, R.layout.widget_linear_layout)
            for (x in colRange) {
                val index = y * colCount + x
                val iconView = RemoteViews(context.packageName, R.layout.widget_icon).apply {
                    setViewPadding(R.id.appIcon_container,
                        viewModel.iconHorizontalGap,
                        viewModel.iconVerticalGap,
                        viewModel.iconHorizontalGap,
                        viewModel.iconVerticalGap)
                }

                // if the last item, there is an error and no title bar, add error dot instead of icon
                val isLastItem = when (viewModel.appSortDirection) {
                    SortingDirection.LeftTop -> { x == colRange.last && y == rowRange.last }
                    SortingDirection.RightTop ->  { x == colRange.first && y == rowRange.last}
                    SortingDirection.LeftBottom -> { x == colRange.last && y == rowRange.first }
                    SortingDirection.RightBottom -> { x == colRange.first && y == rowRange.first }
                    else -> { x == colRange.last && y == rowRange.last }
                }
                if (isLastItem) {
                    val mode = setting.sortAppBy
                    if(viewModel.titleVisibility == View.GONE &&
                        !((mode == SortMode.Count &&
                                PermissionManager.isAccessibilityAllowed(context) &&
                                PermissionManager.isServiceRunning(context)) ||
                                (mode == SortMode.Period &&
                                        PermissionManager.isUsageStatsAllowed(context)) ||
                                (mode == SortMode.Recommend &&
                                        PermissionManager.isAccessibilityAllowed(context) &&
                                        PermissionManager.isServiceRunning(context)) ||
                                (mode == SortMode.Recent &&
                                        PermissionManager.isAccessibilityAllowed(context) &&
                                        PermissionManager.isServiceRunning(context)))
                    ) {
                        iconView.setImageViewResource(R.id.iv_appIcon, R.drawable.warning_service_dot_in_list)

                        val intent = Intent(context, MainActivity::class.java).apply {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                        }
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            0,
                            intent,
                            PendingIntent.FLAG_IMMUTABLE
                        )
                        iconView.setOnClickPendingIntent(R.id.appIcon_container, pendingIntent)
                        linearLayout.addView(R.id.appIconRow, iconView)
                        continue

                    }
                }

                // if no more apps in the list but there is still empty space,
                // add empty iconView layout into the linearLayout
                if (index >= appList.size) {
                    linearLayout.addView(R.id.appIconRow, iconView)
                    continue
                }

                val appInfo = appList[index]
                val pm = context.packageManager
                val packageName = appInfo.packageName
                if (packageName.isEmpty()) continue

                try {
                    pm.getLaunchIntentForPackage(packageName)?.let { launchIntent ->
                        // Get Icon Bitmap
                        val iconStyle = setting.appIconStyle
                        var iconBmp = iconHelper.getAppIcon(packageName, iconStyle)
                        val iconResize = (viewModel.iconSize * iconResizeFactor).toInt()
                        if (iconBmp.width > iconResize || iconBmp.height > iconResize) {
                            val finalIconBmp = iconBmp.resize(iconResize)
                            iconBmp.recycle()
                            iconBmp = finalIconBmp
                        }

                        // Get App Name
                        val name = pm.getApplicationLabel(
                            pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                        )

                        iconView.apply {
                            // Set Icon and name
                            setImageViewBitmap(R.id.iv_appIcon, iconBmp)
                            if (viewModel.isNameShow) {
                                setInt(R.id.tv_appName, "setMinimumHeight", viewModel.appNamePxSize + viewModel.appNameTopMargin)
                                setTextViewText(R.id.tv_appName, name)
                                setTextColor(R.id.tv_appName, viewModel.textColor)
                                setViewVisibility(R.id.tv_appName, View.VISIBLE)
                                setTextViewTextSize(R.id.tv_appName, COMPLEX_UNIT_SP, viewModel.appNameSpSize)
                            } else {
                                setViewVisibility(R.id.tv_appName, View.GONE)
                            }

                            val pendingIntent = PendingIntent.getActivity(
                                context,
                                0,
                                launchIntent,
                                PendingIntent.FLAG_IMMUTABLE)
                            setOnClickPendingIntent(R.id.appIcon_container, pendingIntent)

                        }
                    } ?: continue
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                    continue
                }

                linearLayout.addView(R.id.appIconRow, iconView)
            }

            views.addView(R.id.appIconArea, linearLayout)
        }
    }

    private fun Bitmap.resize(size: Int) : Bitmap {
        val rate: Float
        var newWidth = width
        var newHeight = height
        if (width > height) {
            if (size < width) {
                rate = size / width.toFloat()
                newHeight = (height * rate).toInt()
                newWidth = size
            }
        } else {
            if (size < height) {
                rate = size / height.toFloat()
                newWidth = (width * rate).toInt()
                newHeight = size
            }
        }

        return Bitmap.createScaledBitmap(this, newWidth, newHeight, true)
            .also { this.recycle() }
    }

    companion object {
        private const val iconCountFullResolution = 8
        private const val iconSizeReduceBigFactor = 0.9f
        private const val iconSizeReduceFactor = 0.69f

        private const val iconCountFullResolutionMore = 16
        private const val iconSizeReduceBigFactorMore = 1f
        private const val iconSizeReduceFactorMore = 0.75f

        // Save widget's real sizes. Key is widgetId
        var updatedMinWidths = SparseIntArray()
        var updatedMaxWidths = SparseIntArray()
        var updatedMinHeights = SparseIntArray()
        var updatedMaxHeights = SparseIntArray()

        // duplicated-requested update widget prevention
        private const val UPDATE_WIDGET_IDLE = 0
        private const val UPDATE_WIDGET_WAIT = 1
        private const val UPDATE_WIDGET_GOOD_TO_UPDATE = 2
        private val updateDuplicatePreventionFlags = SparseIntArray()
        private const val UPDATE_WIDGET_DELAY = 300L
    }
}

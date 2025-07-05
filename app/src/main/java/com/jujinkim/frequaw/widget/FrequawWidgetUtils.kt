package com.jujinkim.frequaw.widget

import com.jujinkim.frequaw.FrequawApp
import com.jujinkim.frequaw.R

object FrequawWidgetUtils {

    fun calcColumnCount(widgetWidth: Int,
                        widgetHorizontalPadding: Int,
                        iconAreaWidth: Int,
                        iconHorizontalGap: Int
    ) : Int {
        val colCount =
            ((widgetWidth - widgetHorizontalPadding * 2).toFloat() / (iconAreaWidth + iconHorizontalGap * 2)).toInt()
        return colCount.coerceAtLeast(1)
    }

    fun calcRowCount(widgetHeight: Int,
                     widgetVerticalPadding: Int,
                     iconAreaHeight: Int,
                     iconVerticalGap: Int,
                     isTitleHidden: Boolean
    ) : Int {
        val titleHeight =
            FrequawApp.appContext.resources.getDimensionPixelOffset(R.dimen.widget_title_height)
        val rowCount =
            if (isTitleHidden)
                ((widgetHeight - widgetVerticalPadding * 2).toFloat() / (iconAreaHeight + iconVerticalGap * 2)).toInt()
            else
                ((widgetHeight - titleHeight - widgetVerticalPadding).toFloat() / (iconAreaHeight + iconVerticalGap * 2)).toInt()
        return rowCount.coerceAtLeast(1)
    }

    fun calcWidgetActualHorizontalPadding(widgetWidth: Int,
                                          colCount: Int,
                                          iconAreaWidth: Int,
                                          iconHorizontalGap: Int,
                                          minimumPadding: Int
    ) : Int {
        val horizontalPadding = ((widgetWidth - colCount * (iconAreaWidth + iconHorizontalGap * 2))/2)
        return horizontalPadding.coerceAtLeast(minimumPadding)

    }

    fun calcWidgetActualVerticalPadding(widgetHeight: Int,
                                        rowCount: Int,
                                        iconAreaHeight: Int,
                                        iconVerticalGap: Int,
                                        minimumPadding: Int,
                                        isTitleHidden: Boolean
    ) : Int {
        val titleHeight =
            FrequawApp.appContext.resources.getDimensionPixelOffset(R.dimen.widget_title_height)
        val verticalPadding =
            if (isTitleHidden)
                ((widgetHeight - rowCount * (iconAreaHeight + iconVerticalGap * 2))/2)
            else
                (widgetHeight - titleHeight - rowCount * (iconAreaHeight + iconVerticalGap * 2))
        return verticalPadding.coerceAtLeast(minimumPadding)
    }




}
package com.jujinkim.frequaw

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import com.jujinkim.frequaw.widget.FrequawWidget

object Utils {
    fun Int.dp2px() = (this.toFloat()).dp2px()
    fun Float.dp2px() : Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            FrequawApp.appContext.resources.displayMetrics
        ).toInt()
    }

    fun Int.sp2px() = (this.toFloat()).sp2px()
    fun Float.sp2px() : Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this,
            FrequawApp.appContext.resources.displayMetrics
        ).toInt()
    }

    fun Int.px2sp() = (this.toFloat()).px2sp()
    fun Float.px2sp() : Float {
        return this / FrequawApp.appContext.resources.displayMetrics.scaledDensity
    }

    fun sendUpdateWidgetBr(context: Context) {
        val intent = Intent(context, FrequawWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

            // add widget ids
            val widgetManager = AppWidgetManager.getInstance(context)
            val ids = widgetManager
                .getAppWidgetIds(ComponentName(context, FrequawWidget::class.java))
            widgetManager.notifyAppWidgetViewDataChanged(ids, android.R.id.list)

            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }

        context.sendBroadcast(intent)
    }
}

fun LongArray.getCyclic(index: Int) : Long {
    val idx = if (index < 0) index + this.size else index
    return this[idx % this.size]
}
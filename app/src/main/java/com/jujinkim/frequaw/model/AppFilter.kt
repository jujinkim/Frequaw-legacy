package com.jujinkim.frequaw.model

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.jujinkim.frequaw.FrequawApp

data class AppFilter(
    val packageName: String,
    var isFilter: Boolean
) {
    var icon: Drawable? = null
    var appName: String = ""
}

fun AppFilter.init() : AppFilter {
    val context = FrequawApp.appContext
    if (icon == null) {
        try {
            icon = context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            icon = null
            e.printStackTrace()
        }
    }

    if (appName.isEmpty()) {
        try {
            appName =
                context.packageManager.getApplicationLabel(
                    context.packageManager.getApplicationInfo(
                        packageName,
                        PackageManager.GET_META_DATA)
                ).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            appName = ""
            e.printStackTrace()
        }
    }

    return this
}
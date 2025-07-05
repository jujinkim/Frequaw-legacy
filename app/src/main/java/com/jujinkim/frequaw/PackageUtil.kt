package com.jujinkim.frequaw

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import com.jujinkim.frequaw.data.FrequawDataHelper

@SuppressLint("QueryPermissionsNeeded")
object PackageUtil {
    var homeLauncherPackageName: String = ""
        get() {
            if (field == "") field = acquireHomeLauncherPackageName(null)
            return field
        }

    private val definedHomeLaunchers = listOf(
        "com.google.android.apps.nexuslauncher",    //google
        "com.sec.android.app.launcher"  // samsung
    )

    private val definedIgnores = listOf(
        "com.android.systemui", // system ui
        "com.samsung.android.biometrics.app.setting"    // biometrics (fingerprint, etc.)
    )

    fun isSeemsHomeLauncher(packageName: String) = definedHomeLaunchers.contains(packageName)

    private fun getDefaultKeyboard(context: Context): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)

    private fun acquireHomeLauncherPackageName(context: Context?) : String {
        val pm = context?.packageManager ?: FrequawApp.appContext.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val homePn = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo
            ?.packageName ?: ""

        Log.d(FrequawApp.TAG_DEBUG, "Home : $homePn")

        return homePn
    }

    fun updateHomeLauncherPackageName(context: Context?) {
        homeLauncherPackageName = acquireHomeLauncherPackageName(context)
    }
}
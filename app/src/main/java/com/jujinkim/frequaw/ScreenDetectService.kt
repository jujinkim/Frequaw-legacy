package com.jujinkim.frequaw

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.jujinkim.frequaw.applist.AppListManager
import com.jujinkim.frequaw.applist.AppListRepoAccessibilityService
import java.io.File
import java.text.DateFormat

@SuppressLint("QueryPermissionsNeeded")
class ScreenDetectService : AccessibilityService() {
    private var lastDetectedPackage = ""
    private var lastBrSendTime = 0L
    private val brSendTimePeriod = 10 * 1000L   // update widget per 10sec

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        try {
            val packageName = event.packageName?.toString() ?: ""
            if (packageName.isEmpty()) return

            // check packageName is home launcher or not
            val customLauncherPkgName = PreferenceManager
                .getDefaultSharedPreferences(baseContext)
                .getString("general_set_launcher_package_name", "")
            val isHomeLauncher = (packageName == PackageUtil.homeLauncherPackageName ||
                    PackageUtil.isSeemsHomeLauncher(packageName) ||
                    (!customLauncherPkgName.isNullOrEmpty() && packageName == customLauncherPkgName))

            // Ignore Self
            if (packageName == applicationContext.packageName) return

            // Ignore if the package is not changed
            if (packageName == lastDetectedPackage) {
                Log.d(FrequawApp.TAG_DEBUG, "onAccessibilityEvent ignored(last detected) $packageName")
                return
            }

            // Update package launched count
            lastDetectedPackage = packageName

            AppListManager.accessibilityAppListRepo.updateAndSaveAppInfo(
                packageName,
                System.currentTimeMillis(),
                true
            )
            Log.d(FrequawApp.TAG_DEBUG, "onAccessibilityEvent Update $packageName")

            // if the detect package name is the launcher, force update thd widget
            if (isHomeLauncher && (System.currentTimeMillis() > lastBrSendTime + brSendTimePeriod)) {
                Log.d(FrequawApp.TAG_DEBUG, "Home detected and time to update.. send update br")
                lastBrSendTime = System.currentTimeMillis()
                Utils.sendUpdateWidgetBr(applicationContext)
            }
        } catch (e: Exception) {
            Log.e(FrequawApp.TAG_DEBUG, "onAccessibilityEvent error", e)
            e.printStackTrace()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i("Frequaw", "onServiceConnected")
        PackageUtil.updateHomeLauncherPackageName(applicationContext)
        Toast.makeText(applicationContext, "Frequaw service connected.", Toast.LENGTH_SHORT).show()
    }

    override fun onInterrupt() {
        Log.i(TAG, "onInterrupt")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "onUnbind")
         return super.onUnbind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand")
        return START_STICKY
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun onLowMemory() {
        Log.i(TAG, "onLowMemory")
        super.onLowMemory()
    }

    override fun onRebind(intent: Intent?) {
        Log.i(TAG, "onRebind")
        super.onRebind(intent)
    }

    companion object {
        const val TAG = "ScreenDetectService"
        const val DEBUG_FILENAME = "ServiceDetectDebug.txt"
    }
}
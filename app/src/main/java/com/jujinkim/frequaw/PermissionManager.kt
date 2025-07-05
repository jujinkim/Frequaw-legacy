package com.jujinkim.frequaw

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.os.PowerManager
import android.widget.Toast

object PermissionManager {
    fun isAccessibilityAllowed(context: Context) : Boolean {
        val accessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager?

        return accessibilityManager?.let { manager ->
            val found = manager
                .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
                .indexOfFirst { info -> info.resolveInfo.serviceInfo.packageName == context.packageName }

            return@let found >= 0
        } ?: false
    }

    fun openAccessibilitySettingDialog(context: Context) : AlertDialog {
        val dialog = AlertDialog.Builder(context).apply {
            setTitle(R.string.accessibility_service_label)
            setMessage(R.string.accessibility_service_enable_guide)
            setNegativeButton(R.string.close, null)
            setPositiveButton(R.string.open,
                DialogInterface.OnClickListener { dialog, which ->
                    tryStartActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), context)
                    return@OnClickListener
                }
            )
        }.create()

        dialog.show()
        return dialog
    }

    fun isServiceRunning(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (ScreenDetectService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun isBatteryOptimizationDisabled(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun openDisableBatteryOptimizationDialog(context: Context) {
        val dialog = AlertDialog.Builder(context).apply {
            setTitle(R.string.general_battery_optimization)
            setMessage(R.string.battery_optimization_disable_guide_dialog)
            setNegativeButton(R.string.ignore) { _, _ ->
                SharedPref.setBatteryOptimizationMessageIgnored(true)
            }
            setPositiveButton(R.string.open) { _, _ ->
                tryStartActivity(
                    Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
                    context,
                    context.getString(R.string.battery_optimization_activity_not_found)
                )
            }
        }.create()

        dialog.show()
    }

    fun isUsageStatsAllowed(context: Context): Boolean {
        val manager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            manager.unsafeCheckOpNoThrow(
                OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            manager.checkOpNoThrow(
                OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }

        return mode == MODE_ALLOWED
    }
    fun openUsageStatsPermissionDialog(context: Context): AlertDialog {
        val dialog = AlertDialog.Builder(context).apply {
            setTitle(R.string.general_usage_stats)
            setMessage(R.string.usage_stats_permission_dialog)
            setNegativeButton(R.string.close, null)
            setPositiveButton(R.string.open) { _, _ ->
                openUsageStatsPermissionActivity(context)
            }
        }.create()

        dialog.show()
        return dialog
    }

    fun openUsageStatsPermissionActivity(context: Context) {
        Toast.makeText(context, R.string.usage_stats_permission_toast, Toast.LENGTH_LONG).show()
        tryStartActivity(
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            },
            context
        )
    }

    private fun tryStartActivity(intent: Intent, context: Context, errMsg: String = "") {
        try {
            context.startActivity(intent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            Toast.makeText(
                context,
                context.getString(R.string.general_error_fail_to_open_activity_activitynotfound),
                Toast.LENGTH_SHORT
            ).show()
            if (errMsg.isNotBlank()) {
                Toast.makeText(
                    context,
                    errMsg,
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (ee: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.general_error_fail_to_open_activity_unknown),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
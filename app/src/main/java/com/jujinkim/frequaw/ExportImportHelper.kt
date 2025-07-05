package com.jujinkim.frequaw

import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.jujinkim.frequaw.data.FrequawData
import com.jujinkim.frequaw.data.FrequawDataHelper
import com.jujinkim.frequaw.data.FrequawOldDataUpgrader
import com.jujinkim.frequaw.ui.MainActivity
import java.text.SimpleDateFormat
import java.util.*

class ExportImportHelper(val activity: MainActivity) {

    // Export result listener
    private val activityResultCreateDoc =
        activity.registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
            try {
                val output = activity.contentResolver.openOutputStream(uri!!)
                output?.bufferedWriter().use { it?.write(Gson().toJson(FrequawDataHelper.load())) }
                Toast.makeText(
                    activity,
                    R.string.general_export_import_exported_done,
                    Toast.LENGTH_SHORT
                )
            } catch(e: Exception) {
                Toast.makeText(
                    activity,
                    R.string.general_export_import_setting_fail_to_export,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    // Import result listener
    private val activityResultOpenDoc =
        activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            try {
                val input = activity.contentResolver.openInputStream(uri!!)
                val jsonStr = input?.reader()?.readText() ?: ""

                AlertDialog.Builder(activity)
                    .setMessage(R.string.general_export_import_setting_import_double_check)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        val map = HashMap<String, Any>()
                        val data = Gson().fromJson(jsonStr, map::class.java)
                        val frequawData = if (!data.containsKey("dataVersion")) {
                            if (data.containsKey("a")) {
                                FrequawOldDataUpgrader.restoreFromV1ObfuscatedData(jsonStr)
                            } else {
                                FrequawOldDataUpgrader.upgradeFromOldJson(jsonStr)
                            }
                        } else {
                            FrequawDataHelper.gsonFromJsonAndFitVersion(jsonStr)
                        }
                        if (frequawData != FrequawData.default()) {
                            Toast.makeText(
                                activity,
                                R.string.general_export_import_imported_done,
                                Toast.LENGTH_SHORT
                            )

                            // Save original widgetSettings to the new Data
                            FrequawDataHelper.load().widgetSettings.forEach {
                                frequawData.widgetSettings.containsKey(it.key).let { hasKey ->
                                    if (!hasKey) {
                                        // Put current widgetSettings if it doesn't have the key
                                        frequawData.widgetSettings[it.key] = it.value
                                    }
                                }
                            }
                            // Save loaded data
                            FrequawDataHelper.save(frequawData)

                            val intent = activity.intent
                            activity.finish()
                            activity.startActivity(intent)
                        } else {
                            throw Exception()
                        }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()

            } catch (e: Exception) {
                Toast.makeText(
                    activity,
                    R.string.general_export_import_setting_fail_to_import,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    fun export() {
        val fileName =
            "FrequawSetting-${SimpleDateFormat("ddMMyyyy-hhmmss", Locale.getDefault()).format(Date())}.txt"
        activityResultCreateDoc.launch(fileName)
    }

    fun import() {
        Toast.makeText(
            activity,
            R.string.general_export_import_select_setting_file_from_frequaw,
            Toast.LENGTH_SHORT
        ).show()
        activityResultOpenDoc.launch(arrayOf("text/plain"))
    }
}
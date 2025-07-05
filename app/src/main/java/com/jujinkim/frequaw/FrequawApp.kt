package com.jujinkim.frequaw

import android.app.Application
import android.content.Context
import com.jujinkim.frequaw.data.FrequawOldDataUpgrader

class FrequawApp : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        FrequawOldDataUpgrader.upgradeFromSharedPrefAndSave()
    }

    companion object {
        lateinit var appContext: Context

        var TAG_DEBUG = "FREQUAWTEST"
    }

}
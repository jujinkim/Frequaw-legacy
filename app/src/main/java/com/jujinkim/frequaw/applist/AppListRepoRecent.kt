package com.jujinkim.frequaw.applist

import android.util.Log
import com.jujinkim.frequaw.FrequawApp
import com.jujinkim.frequaw.model.AppInfo

class AppListRepoRecent (
    private val repoAccessibilityService: AppListRepoAccessibilityService,
) : AppListRepo by repoAccessibilityService {
    override fun getAppList(): List<AppInfo> {
        Log.d(FrequawApp.TAG_DEBUG, "->Get App List from Recent Repo")

        return repoAccessibilityService.getAppList().onEach { it.sortValue = it.lastLaunched }
    }
}
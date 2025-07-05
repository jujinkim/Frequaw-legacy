package com.jujinkim.frequaw.applist

import com.jujinkim.frequaw.model.AppInfo

interface AppListRepo {
    fun getAppList(): List<AppInfo>
    fun updateAndSaveAppInfo(packageName: String,
                             lastLaunched: Long,
                             ignoreInMomentRequest: Boolean)
    fun saveAppList(apps: List<AppInfo>)
    fun clearAppList()
    fun resetOnAppInfo(packageName: String)
}
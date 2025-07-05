package com.jujinkim.frequaw.model

import android.graphics.drawable.Drawable
import com.jujinkim.frequaw.FrequawApp
import com.jujinkim.frequaw.R

data class IconPackItem(
    val packageName: String,
    val name: String,
    val icon: Drawable?
) {
    companion object {
        fun DefaultModel() = IconPackItem(
            "",
            FrequawApp.appContext.getString(R.string.icon_style_no_icon_pack),
            null
        )
    }
}

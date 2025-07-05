package com.jujinkim.frequaw.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.jujinkim.frequaw.AppIconStyle
import com.jujinkim.frequaw.FrequawApp
import com.jujinkim.frequaw.R
import com.jujinkim.frequaw.data.FrequawData
import com.jujinkim.frequaw.data.FrequawDataHelper
import com.jujinkim.frequaw.data.FrequawWidgetSettingData
import com.jujinkim.frequaw.model.IconPackItem

@Composable
fun SettingsVisualIconStyleComposable(data: FrequawData, widgetId: Int) = Column {
    val widgetData = data.getWidgetSetting(widgetId)
    var selectedShape by remember { mutableStateOf(widgetData.appIconStyle) }
    var isForceShape by remember { mutableStateOf(widgetData.isForceIconShapeClip) }
    var selectedIconPkg by remember { mutableStateOf(widgetData.appIconPackPackage) }
    val iconPackList by remember { mutableStateOf(loadIconPackList()) }


    // System shape
    RadioButtonIconText(
        text = stringResource(R.string.icon_style_use_system_default),
        selected = selectedShape == AppIconStyle.System,
        onClick = {
            selectedShape = AppIconStyle.System
            saveIconStyle(widgetData, selectedShape, isForceShape, selectedIconPkg)
        }
    )
    Row(modifier = Modifier.fillMaxWidth()) {
        // Circle
        RadioButtonIconText(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.icon_style_circle),
            selected = selectedShape == AppIconStyle.Circle,
            onClick = {
                selectedShape = AppIconStyle.Circle
                saveIconStyle(widgetData, selectedShape, isForceShape, selectedIconPkg)
            }
        )
        // Squircle
        RadioButtonIconText(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.icon_style_squircle),
            selected = selectedShape == AppIconStyle.Squircle,
            onClick = {
                selectedShape = AppIconStyle.Squircle
                saveIconStyle(widgetData, selectedShape, isForceShape, selectedIconPkg)
            }
        )
    }
    Row(modifier = Modifier.fillMaxWidth()) {
        // Square
        RadioButtonIconText(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.icon_style_square),
            selected = selectedShape == AppIconStyle.Square,
            onClick = {
                selectedShape = AppIconStyle.Square
                saveIconStyle(widgetData, selectedShape, isForceShape, selectedIconPkg)
            }
        )
        // Rounded rectangle
        RadioButtonIconText(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.icon_style_rounded_squire),
            selected = selectedShape == AppIconStyle.RoundedSquare,
            onClick = {
                selectedShape = AppIconStyle.RoundedSquare
                saveIconStyle(widgetData, selectedShape, isForceShape, selectedIconPkg)
            }
        )
    }

    // Icon pack
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .background(colorResource(R.color.icon_pack_list_bg), RoundedCornerShape(8.dp))
    ) {
        items(iconPackList) { iconPack ->
            RadioButtonIconText(
                icon = iconPack.icon,
                text = iconPack.name,
                selected = selectedIconPkg == iconPack.packageName,
                onClick = {
                    selectedIconPkg = iconPack.packageName
                    saveIconStyle(widgetData, selectedShape, isForceShape, selectedIconPkg)
                }
            )
        }
    }

    // Force clip
    Row(modifier = Modifier.fillMaxWidth()) {
        Checkbox(
            checked = isForceShape,
            onCheckedChange = {
                isForceShape = it
                saveIconStyle(widgetData, selectedShape, isForceShape, selectedIconPkg)
            }
        )
        Text(
            text = stringResource(R.string.icon_style_apply_shape_force),
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun RadioButtonIconText(
    modifier: Modifier = Modifier,
    icon: Drawable? = null,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) = Row(
    modifier = modifier
        .height(40.dp)
        .padding(4.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
    }
    if (icon != null) {
        Image(bitmap = icon.toBitmap().asImageBitmap(), contentDescription = null)
        Spacer(modifier = Modifier.width(4.dp))
    }
    Text(
        text = text,
        fontSize = 14.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.clickable(onClick = onClick)
    )
}

private fun saveIconStyle(
    widgetSettingData: FrequawWidgetSettingData,
    style: AppIconStyle,
    isForceShape: Boolean,
    iconPackPkgName: String
) {
    widgetSettingData.appIconStyle = style
    widgetSettingData.isForceIconShapeClip = isForceShape
    widgetSettingData.appIconPackPackage = iconPackPkgName

    FrequawDataHelper.saveWidgetSetting(widgetSettingData)
}

private fun loadIconPackList(): List<IconPackItem> {
    val iconPackList = hashMapOf<String, IconPackItem>().apply {
        val context = FrequawApp.appContext
        put("", IconPackItem.DefaultModel())

        val iconPackIntentFilterList = listOf(
            "org.adw.launcher.THEMES",
            "com.gau.go.launcherex.theme"
        )
        val pm = context.packageManager

        val resolveInfos = mutableListOf<ResolveInfo>().apply {
            iconPackIntentFilterList.forEach { intentFilter ->
                addAll(pm.queryIntentActivities(Intent(intentFilter), PackageManager.GET_META_DATA))
            }
        }

        resolveInfos.forEach { resolveInfo ->
            try {
                val iconPackPkgName = resolveInfo.activityInfo.packageName
                val ai = pm.getApplicationInfo(iconPackPkgName, PackageManager.GET_META_DATA)

                val iconPack = IconPackItem(
                    iconPackPkgName,
                    pm.getApplicationLabel(ai).toString(),
                    pm.getApplicationIcon(ai)
                )

                put(iconPackPkgName, iconPack)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                return@forEach
            }
        }
    }.values.toMutableList()

    return iconPackList
}
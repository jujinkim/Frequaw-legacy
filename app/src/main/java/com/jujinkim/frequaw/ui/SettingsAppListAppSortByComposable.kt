package com.jujinkim.frequaw.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jujinkim.frequaw.*
import com.jujinkim.frequaw.R
import com.jujinkim.frequaw.data.FrequawData
import com.jujinkim.frequaw.data.FrequawDataHelper
import com.jujinkim.frequaw.data.FrequawWidgetSettingData

@Composable
fun SettingsAppListAppSortByComposable(
    data: FrequawData,
    widgetId: Int,
    onModeChanged: (SortMode) -> Unit
) = Column(
    modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
) {
    val widgetData = data.getWidgetSetting(widgetId)
    var selectedMode by remember { mutableStateOf(widgetData.sortAppBy) }
    var selectedRcmdMode by remember { mutableStateOf(widgetData.recommendSortMode) }

    // Recommend
    SettingBigItemComposable(
        text = stringResource(R.string.select_detect_mode_recommend),
        description = stringResource(R.string.select_detect_mode_recommend_desc),
        selected = selectedMode == SortMode.Recommend,
        onClick = {
            selectedMode = SortMode.Recommend
            saveMode(widgetData, selectedMode)
            onModeChanged(selectedMode)
        },
        innerContent = {
            Column {
                Text(text = stringResource(R.string.select_detect_mode_recommend_priority_title))
                // Launched Time
                SettingBigItemInnerComposable(
                    text = stringResource(id = R.string.select_detect_mode_recommend_priority_time),
                    selected = selectedRcmdMode == RcmdSortMode.Time,
                    onClick = {
                        selectedMode = SortMode.Recommend
                        selectedRcmdMode = RcmdSortMode.Time
                        saveRecommendMode(widgetData, selectedRcmdMode)
                        saveMode(widgetData, selectedMode)
                        onModeChanged(selectedMode)
                    }
                )

                // Balanced
                SettingBigItemInnerComposable(
                    text = stringResource(id = R.string.select_detect_mode_recommend_priority_balanced),
                    selected = selectedRcmdMode == RcmdSortMode.Balanced,
                    onClick = {
                        selectedMode = SortMode.Recommend
                        selectedRcmdMode = RcmdSortMode.Balanced
                        saveRecommendMode(widgetData, selectedRcmdMode)
                        saveMode(widgetData, selectedMode)
                        onModeChanged(selectedMode)
                    }
                )

                // Launched Count
                SettingBigItemInnerComposable(
                    text = stringResource(id = R.string.select_detect_mode_recommend_priority_count),
                    selected = selectedRcmdMode == RcmdSortMode.Count,
                    onClick = {
                        selectedMode = SortMode.Recommend
                        selectedRcmdMode = RcmdSortMode.Count
                        saveRecommendMode(widgetData, selectedRcmdMode)
                        saveMode(widgetData, selectedMode)
                        onModeChanged(selectedMode)
                    }
                )
            }
        }
    )

    // Launched count
    SettingBigItemComposable(
        text = stringResource(R.string.select_detect_mode_launch_count),
        description = stringResource(R.string.select_detect_mode_launch_count_desc),
        selected = selectedMode == SortMode.Count,
        onClick = {
            selectedMode = SortMode.Count
            saveMode(widgetData, selectedMode)
            onModeChanged(selectedMode)
        }
    )

    // Usage time
    SettingBigItemComposable(
        text = stringResource(R.string.select_detect_mode_usage_time),
        description = stringResource(R.string.select_detect_mode_usage_time_desc),
        selected = selectedMode == SortMode.Period,
        onClick = {
            selectedMode = SortMode.Period
            saveMode(widgetData, selectedMode)
            onModeChanged(selectedMode)
        }
    )

    // Recently used
    SettingBigItemComposable(
        text = stringResource(R.string.select_detect_mode_recent),
        description = stringResource(R.string.select_detect_mode_recent_desc),
        selected = selectedMode == SortMode.Recent,
        onClick = {
            selectedMode = SortMode.Recent
            saveMode(widgetData, selectedMode)
            onModeChanged(selectedMode)
        }
    )
}

@Composable
fun SettingBigItemInnerComposable(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) = Text(
    text = text,
    textAlign = TextAlign.Center,
    modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp)
        .border(
            1.5.dp,
            if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface,
            RoundedCornerShape(8.dp)
        )
        .clip(RoundedCornerShape(8.dp))
        .clickable(onClick = onClick)
        .padding(4.dp)
)

private fun saveMode(widgetSettingData: FrequawWidgetSettingData, mode: SortMode) {
    val setting = widgetSettingData.apply {
        sortAppBy = mode
    }
    FrequawDataHelper.saveWidgetSetting(setting)
}

private fun saveRecommendMode(widgetSettingData: FrequawWidgetSettingData, mode: RcmdSortMode) {
    val setting = widgetSettingData.apply {
        recommendSortMode = mode
    }
    FrequawDataHelper.saveWidgetSetting(setting)
}
package com.jujinkim.frequaw.ui

import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jujinkim.frequaw.R
import com.jujinkim.frequaw.Screen
import com.jujinkim.frequaw.data.FrequawData
import com.jujinkim.frequaw.data.FrequawDataHelper
import com.jujinkim.frequaw.viewmodel.SettingViewModel

@Composable
fun SettingsVisualComposable(
    data: FrequawData,
    widgetId: Int,
    viewModel: SettingViewModel = viewModel()
) {
    val widgetData = data.getWidgetSetting(widgetId)
    val context = LocalContext.current

    var isShowAppsName by remember { mutableStateOf(widgetData.isShowAppsName) }
    var isSetDarkModeColor by remember { mutableStateOf(widgetData.isSetDarkModeColor) }
    var isAdvancedWidgetLayout by remember { mutableStateOf(widgetData.isAdvancedWidgetLayout) }
    var advLytIsHorVerIconGapSeparate by remember { mutableStateOf(widgetData.advLytIsHorVerIconGapSeparate) }
    var advLytIsHorVerListPaddingSeparate by remember { mutableStateOf(widgetData.advLytIsHorVerListPaddingSeparate) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            SettingListTitleComposable(stringResource(R.string.header_visualization))
        }

        // Title bar
        item {
            SettingListItemToggleComposable(
                text = stringResource(R.string.visual_title_visible),
                description = stringResource(R.string.visual_title_visible_desc_on),
                checked = widgetData.isTitleVisible,
                onCheckedChange = {
                    widgetData.isTitleVisible = it
                    FrequawDataHelper.saveWidgetSetting(widgetData)
                    viewModel.updateWidgetPreviewEvent.call()
                }
            )
        }

        // Title text
        item {
            SettingListItemTextFieldComposable(
                text = stringResource(R.string.visual_title_text),
                value = widgetData.titleText,
                placeHolder = stringResource(R.string.visual_title_text_empty),
                onTextChanged = {
                    widgetData.titleText = it
                    FrequawDataHelper.saveWidgetSetting(widgetData)
                },
            )
        }

        // Show update time
        item {
            SettingListItemToggleComposable(
                text = stringResource(R.string.visual_title_update_time_visible),
                description = stringResource(R.string.visual_title_update_time_visible_desc_on),
                checked = widgetData.isTitleUpdateTimeVisible,
                onCheckedChange = {
                    widgetData.isTitleUpdateTimeVisible = it
                    FrequawDataHelper.saveWidgetSetting(widgetData)
                }
            )
        }

        // App icon style
        item {
            SettingListItemDescriptionComposable(
                text = stringResource(R.string.visual_icon_style),
                description = stringResource(R.string.visual_icon_style_desc),
                onClick = {
                    viewModel.navigate(Screen.VisualIconStyle)
                }
            )
        }

        // App icon size
        item {
            SettingListItemSliderComposable(
                text = stringResource(R.string.visual_icon_size),
                value = widgetData.appIconSize,
                range = 30..70,
                onValueChange = {
                    widgetData.appIconSize = it
                    FrequawDataHelper.saveWidgetSetting(widgetData)
                    viewModel.updateWidgetPreviewEvent.call()
                }
            )
        }

        // show app name
        item {
            SettingListItemToggleComposable(
                text = stringResource(R.string.visual_show_app_name),
                description = stringResource(R.string.visual_show_app_name),
                checked = widgetData.isShowAppsName,
                onCheckedChange = {
                    isShowAppsName = it
                    widgetData.isShowAppsName = it
                    FrequawDataHelper.saveWidgetSetting(widgetData)
                    viewModel.updateWidgetPreviewEvent.call()
                }
            )
        }

        if (isShowAppsName) {
            // App name size
            item {
                SettingListItemSliderComposable(
                    text = stringResource(R.string.visual_app_name_size),
                    value = widgetData.appsNameSize,
                    range = 5..25,
                    onValueChange = {
                        widgetData.appsNameSize = it
                        FrequawDataHelper.saveWidgetSetting(widgetData)
                    }
                )
            }

            // App name top margin
            item {
                SettingListItemSliderComposable(
                    text = stringResource(R.string.visual_app_name_margin),
                    value = widgetData.appsNameMargin,
                    range = 0..15,
                    onValueChange = {
                        widgetData.appsNameMargin = it
                        FrequawDataHelper.saveWidgetSetting(widgetData)
                    }
                )
            }
        }

        // Background corner radius
        item {
            SettingListItemSliderComposable(
                text = stringResource(R.string.visual_bg_corner_radius),
                value = widgetData.backgroundCornerRadius,
                range = 0..30,
                onValueChange = {
                    widgetData.backgroundCornerRadius = it
                    FrequawDataHelper.saveWidgetSetting(widgetData)
                }
            )
        }

        // Enable dark mode color
        item {
            SettingListItemToggleComposable(
                text = stringResource(R.string.visual_set_dark_mode_color),
                description = stringResource(R.string.visual_set_dark_mode_color),
                checked = widgetData.isSetDarkModeColor,
                onCheckedChange = {
                    isSetDarkModeColor = it
                    widgetData.isSetDarkModeColor = it
                    FrequawDataHelper.saveWidgetSetting(widgetData)
                }
            )
        }

        // Background color
        item {
            SettingListItemColorComposable(
                text = stringResource(R.string.visual_bg_color),
                color = widgetData.backgroundColor,
                onColorChange = {
                    widgetData.backgroundColor = it
                    FrequawDataHelper.saveWidgetSetting(widgetData)
                }
            )
        }

        // Text color
        item {
            SettingListItemColorComposable(
                text = stringResource(R.string.visual_app_name_color),
                color = widgetData.textColor,
                onColorChange = {
                    widgetData.textColor = it
                    FrequawDataHelper.saveWidgetSetting(widgetData)
                }
            )
        }

        if (isSetDarkModeColor) {
            // Dark mode background color
            item {
                SettingListItemColorComposable(
                    text = stringResource(R.string.visual_bg_color_dark),
                    color = widgetData.backgroundDarkModeColor,
                    onColorChange = {
                        widgetData.backgroundDarkModeColor = it
                        FrequawDataHelper.saveWidgetSetting(widgetData)
                    }
                )
            }

            // Dark mode text color
            item {
                SettingListItemColorComposable(
                    text = stringResource(R.string.visual_app_name_color_dark),
                    color = widgetData.textDarkModeColor,
                    onColorChange = {
                        widgetData.textDarkModeColor = it
                        FrequawDataHelper.saveWidgetSetting(widgetData)
                    }
                )
            }
        }

        // Advanced visual custom setting
        item {
            SettingListItemToggleComposable(
                text = stringResource(R.string.visual_advanced_enabled),
                description = stringResource(R.string.visual_advanced_enabled_desc_on),
                checked = widgetData.isAdvancedWidgetLayout,
                onCheckedChange = {
                    isAdvancedWidgetLayout = it
                    widgetData.isAdvancedWidgetLayout = it
                    FrequawDataHelper.saveWidgetSetting(widgetData)

                    if (it) viewModel.widgetPreviewVisibility.value = View.VISIBLE
                    else viewModel.widgetPreviewVisibility.value = View.GONE
                    viewModel.updateWidgetPreviewEvent.call()
                }
            )
        }

        if (isAdvancedWidgetLayout) {
            item {
                SettingListTitleComposable(stringResource(R.string.header_visualization_advanced))
            }

            // Separate setting of app icon horizontal&vertical gap
            item {
                SettingListItemToggleComposable(
                    text = stringResource(R.string.visual_icon_gap_set_separately),
                    description = stringResource(R.string.visual_icon_gap_set_separately),
                    checked = widgetData.advLytIsHorVerIconGapSeparate,
                    onCheckedChange = {
                        advLytIsHorVerIconGapSeparate = it
                        widgetData.advLytIsHorVerIconGapSeparate = it
                        FrequawDataHelper.saveWidgetSetting(widgetData)
                        viewModel.updateWidgetPreviewEvent.call()
                    }
                )
            }

            // App icon horizontal gap
            item {
                SettingListItemSliderComposable(
                    text = stringResource(
                        if (advLytIsHorVerIconGapSeparate) R.string.visual_icon_gap_horizontal_margin
                        else R.string.visual_icon_gap
                    ),
                    value = widgetData.advLytHorIconGapSize,
                    range = 0..40,
                    onValueChange = {
                        widgetData.advLytHorIconGapSize = it
                        FrequawDataHelper.saveWidgetSetting(widgetData)
                        viewModel.updateWidgetPreviewEvent.call()
                    }
                )
            }

            // App icon vertical gap
            item {
                if (advLytIsHorVerIconGapSeparate) {
                    SettingListItemSliderComposable(
                        text = stringResource(R.string.visual_icon_gap_vertical_margin),
                        value = widgetData.advLytVerIconGapSize,
                        range = 0..40,
                        onValueChange = {
                            widgetData.advLytVerIconGapSize = it
                            FrequawDataHelper.saveWidgetSetting(widgetData)
                            viewModel.updateWidgetPreviewEvent.call()
                        }
                    )
                }
            }

            // Separate setting of app list horizontal&vertical padding
            item {
                SettingListItemToggleComposable(
                    text = stringResource(R.string.visual_list_margin_set_separately),
                    description = stringResource(R.string.visual_list_margin_set_separately),
                    checked = widgetData.advLytIsHorVerListPaddingSeparate,
                    onCheckedChange = {
                        advLytIsHorVerListPaddingSeparate = it
                        widgetData.advLytIsHorVerListPaddingSeparate = it
                        FrequawDataHelper.saveWidgetSetting(widgetData)
                        viewModel.updateWidgetPreviewEvent.call()
                    }
                )
            }

            // App list horizontal padding
            item {
                SettingListItemSliderComposable(
                    text = stringResource(
                        if (advLytIsHorVerListPaddingSeparate) R.string.visual_list_horizontal_margin
                        else R.string.visual_list_margin
                    ),
                    value = widgetData.advLytHorListPaddingSize,
                    range = 0..40,
                    onValueChange = {
                        widgetData.advLytHorListPaddingSize = it
                        FrequawDataHelper.saveWidgetSetting(widgetData)
                        viewModel.updateWidgetPreviewEvent.call()
                    }
                )
            }

            // App list vertical padding
            if (advLytIsHorVerListPaddingSeparate) {
                item {
                    SettingListItemSliderComposable(
                        text = stringResource(R.string.visual_list_vertical_margin),
                        description = stringResource(R.string.visual_list_vertical_margin_if_title_only_bottom_desc),
                        value = widgetData.advLytVerListPaddingSize,
                        range = 0..40,
                        onValueChange = {
                            widgetData.advLytVerListPaddingSize = it
                            FrequawDataHelper.saveWidgetSetting(widgetData)
                            viewModel.updateWidgetPreviewEvent.call()
                        }
                    )
                }
            }

            // About App list padding
            item {
                SettingListItemDescriptionComposable(
                    text = stringResource(R.string.visual_list_about_margin),
                    description = stringResource(R.string.visual_list_about_margin_desc),
                    onClick = {
                        // todo : Show visual_list_about_margin_desc dialog
                    }
                )
            }
        }
    }
}
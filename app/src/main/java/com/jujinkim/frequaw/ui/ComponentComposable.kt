package com.jujinkim.frequaw.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.toColorInt
import com.jujinkim.frequaw.R
import kotlin.math.roundToInt

@Preview
@Composable
fun ComponentsPreview() {
    Column {
        SettingListTitleComposable("This is title")
        SettingListItemDescriptionComposable("Title", "This is description")
        SettingListItemDropdownComposable(
            text = "dropdown",
            items = mapOf("first" to "f", "second" to "s"),
            selectedKey = "first",
            onItemChanged = {})
        SettingListItemSliderComposable(
            text = "slider",
            value = 5,
            range = 3..10,
            onValueChange = {})
        SettingListItemTextFieldComposable(text = "textField", value = "value", onTextChanged = {})
        SettingListItemToggleComposable(
            text = "toggle",
            description = "tototoglgl",
            checked = false,
            onCheckedChange = {})
        SettingListItemPermissionCheckComposable(
            text = "checkbox",
            description = "descriptions",
            granted = { true })
        SettingListItemColorComposable(
            text = "color",
            description = "desc",
            color = Color.Black.toArgb(),
            onColorChange = {})
    }
}

@Composable
fun SettingListTitleComposable(text: String) {
    Text(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        text = text,
        style = MaterialTheme.typography.h6
    )
}

@Composable
fun SettingListItemComposable(
    modifier: Modifier = Modifier,
    text: String,
    description: String = "",
    content: @Composable () -> Unit = {}
) = Column(
    modifier = modifier
        .padding(12.dp)
        .fillMaxWidth()
) {
    Text(
        text = text,
        style = MaterialTheme.typography.body1,
    )
    if (description.isNotBlank()) {
        Text(
            text = description,
            style = MaterialTheme.typography.body2
        )
    }
    content()
}

@Composable
fun SettingListItemDescriptionComposable(
    text: String,
    description: String = "",
    onClick: () -> Unit = {}
) = SettingListItemComposable(
    modifier = Modifier.clickable {
        onClick()
    },
    text = text,
    description = description,
)

@Composable
fun SettingListItemTextFieldComposable(
    text: String,
    description: String = "",
    value: String,
    label: String = "",
    placeHolder: String = "",
    onTextChanged: (String) -> Unit
) = SettingListItemComposable(text = text, description = description) {
    var txt by remember { mutableStateOf(value) }
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = txt,
        label = if (label.isNotBlank()) {
            { Text(label) }
        } else null,
        placeholder = if (placeHolder.isNotBlank()) {
            { Text(placeHolder) }
        } else null,
        onValueChange = {
            txt = it
            onTextChanged(it)
        },
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingListItemToggleComposable(
    text: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    var checkedState by remember { mutableStateOf(checked) }

    val modifier = Modifier.clickable {
        checkedState = !checkedState
        onCheckedChange(checkedState)
    }

    SettingListItemComposable(text = text, modifier = modifier) {

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = description,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.body2
            )
            CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
                Switch(checked = checkedState, onCheckedChange = {
                    checkedState = it
                    onCheckedChange(it)
                })
            }
        }
    }
}

@Composable
fun SettingListItemDropdownComposable(
    text: String,
    description: String = "",
    items: Map<String, String>,
    selectedKey: String,
    onItemChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(items[selectedKey]) }

    val modifier = Modifier.clickable { expanded = true }

    SettingListItemComposable(text = text, description = description, modifier = modifier) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = selectedItem ?: "",
                style = MaterialTheme.typography.body2
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                items.forEach { (key, value) ->
                    DropdownMenuItem(onClick = {
                        expanded = false
                        selectedItem = value
                        onItemChanged(key)
                    }) {
                        Text(text = value)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingListItemSliderComposable(
    text: String,
    description: String = "",
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) = SettingListItemComposable(text = text, description = description) {
    var sliderValue by remember { mutableStateOf(value) }
    Row {
        Slider(
            modifier = Modifier.weight(1f),
            value = sliderValue.toFloat(),
            onValueChange = {
                sliderValue = it.roundToInt()
                onValueChange(it.roundToInt())
            },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = range.last - range.first
        )
        Text(
            modifier = Modifier
                .width(50.dp)
                .padding(8.dp),
            text = sliderValue.toString(),
        )
    }
}

@Composable
fun SettingListItemColorComposable(
    text: String,
    description: String = "",
    color: Int,
    onColorChange: (Int) -> Unit
) {
    var openDialog by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(Color(color)) }
    var selectedColorString by remember { mutableStateOf(Integer.toHexString(color)) }
    val modifier = Modifier.clickable { openDialog = true }

    SettingListItemComposable(text = text, modifier = modifier) {

        if (openDialog) {
            Dialog(
                onDismissRequest = { openDialog = false }
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 320.dp)
                        .heightIn(max = 480.dp)
                        .background(MaterialTheme.colors.background, MaterialTheme.shapes.small)
                        .padding(8.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        value = selectedColorString,
                        onValueChange = {
                            selectedColorString = it
                            try {
                                val colorInt = android.graphics.Color.parseColor("#$it")
                                selectedColor = Color(colorInt)
                                onColorChange(colorInt)
                            } catch (e: Exception) {
                                // ignore
                            }
                        },
                        label = {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.h6
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    ClassicColorPicker(
                        modifier = Modifier.weight(1f),
                        color = selectedColor
                    ) {
                        onColorChange(it.toColorInt())
                        selectedColor = it.toColor()
                        selectedColorString = Integer.toHexString(it.toColorInt())
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            openDialog = false
                        }
                    ) {
                        Text(text = "Close")
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = description,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.body2
            )
            Box(
                modifier = Modifier
                    .width(35.dp)
                    .height(35.dp)
                    .background(color = selectedColor)
                    .border(1.dp, Color.Gray)
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingListItemPermissionCheckComposable(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    text: String,
    description: String,
    granted: () -> Boolean,
    onClick: () -> Unit = {}
) = SettingListItemComposable(
    modifier = Modifier.clickable {
        onClick()
    },
    text = text
) {
    var isGranted by remember { mutableStateOf(granted()) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { source, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isGranted = granted()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = description,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.body2
        )
        Spacer(modifier = Modifier.width(8.dp))
        CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
            Checkbox(isGranted, {})
        }
    }
}

// SortAppBy(DetectMode), ...
@Composable
fun SettingBigItemComposable(
    text: String,
    description: String,
    innerContent: @Composable() (() -> Unit)? = null,
    selected: Boolean,
    onClick: () -> Unit
) = Column(
    modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp, 6.dp)
        .border(
            2.dp,
            if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface,
            RoundedCornerShape(16.dp)
        )
        .clip(RoundedCornerShape(16.dp))
        .clickable(onClick = onClick)
        .padding(12.dp)
) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = description
    )

    if (innerContent != null) {
        Spacer(modifier = Modifier.height(8.dp))
        innerContent()
    }
}

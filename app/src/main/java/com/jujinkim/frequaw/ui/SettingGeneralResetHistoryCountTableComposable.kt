package com.jujinkim.frequaw.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jujinkim.frequaw.model.AppInfo

@Composable
fun SettingsGeneralResetHistoryCountTableComposable(
    appInfo: AppInfo?,
    onCloseClick: () -> Unit
) = Column(
    modifier = Modifier
        .width(320.dp)
        .fillMaxHeight()
        .background(color = if (isSystemInDarkTheme()) Color.Black else Color.White)
        .padding(12.dp)
) {
    appInfo ?: return@Column
    fun createTimeString(idx: Int): String {
        val hour = idx / 2
        val minute = if (idx % 2 == 0) "00" else "30"
        return "$hour:$minute"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = appInfo.appName()
        )
        IconButton(
            modifier = Modifier.size(36.dp),
            onClick = onCloseClick
        ) {
            Icon(Icons.Rounded.Close, contentDescription = "close")
        }
    }

    val dayOfWeeks = listOf("", "S", "M", "T", "W", "T", "F", "S")
    Row(modifier = Modifier.fillMaxWidth()) {
        repeat(8) {
            HistoryCountTableCell(text = dayOfWeeks[it], color = Color.Red, modifier = Modifier.weight(1f))
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().weight(1f)
    ) {
        items((0 until appInfo.launchedCountsBy30m.size / 7).toList()) { hourminute ->
            Row {
                HistoryCountTableCell(
                    text = createTimeString(hourminute),
                    color = Color.Blue,
                    modifier = Modifier.weight(1f)
                )
                repeat(7) { dot ->
                    HistoryCountTableCell(
                        text = appInfo.getCountOf30m(dot, hourminute / 2, hourminute % 2)
                            .toString(),
                        color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

}

@Composable
private fun HistoryCountTableCell(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) =  Text(
    modifier = modifier
        .border(width = 0.5.dp, color = Color.Gray)
        .height(24.dp),
    text = text,
    textAlign = TextAlign.Center,
    color = color,
)

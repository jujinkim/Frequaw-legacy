package com.jujinkim.frequaw.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jujinkim.frequaw.R
import com.jujinkim.frequaw.viewmodel.SettingViewModel

@Composable
fun SettingsNoSettingComposable(
    viewModel: SettingViewModel = viewModel()
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val intent = Intent(
            context,
            MainActivity::class.java
        )

        Text(
            text = stringResource(id = R.string.this_widget_following_default)
        )
        
        Button(onClick = {
            viewModel.openDefaultSetting(intent)
        }) {
            Text(
                text = stringResource(id = R.string.open_default_setting)
            )
        }
        
        Button(onClick = {
            viewModel.createWidgetSetting(intent)
        }) {
            Text(
                text = stringResource(id = R.string.create_new_setting)
            )
        }

    }

}
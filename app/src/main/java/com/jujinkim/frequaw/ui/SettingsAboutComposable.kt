package com.jujinkim.frequaw.ui

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.jujinkim.frequaw.BuildConfig
import com.jujinkim.frequaw.R
import com.jujinkim.frequaw.SharedPref

@Preview
@Composable
fun SettingsAboutPreview() {
    SettingsAboutComposable()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsAboutComposable() = Column(
    modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
) {
    val context = LocalContext.current

    Spacer(modifier = Modifier.padding(16.dp))
    // App Icon
    Image(
        bitmap =
        context.packageManager.getApplicationIcon(BuildConfig.APPLICATION_ID)
            ?.toBitmap()
            ?.asImageBitmap() ?: ImageBitmap(1, 1),
        contentDescription = "Frequaw App Icon",
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    val debug = SharedPref.toggleDebugMode()
                    Toast.makeText(context, "DEBUG MODE : $debug", Toast.LENGTH_SHORT).show()
                }
            )
    )

    // Version
    Text(
        text = BuildConfig.VERSION_NAME,
        style = MaterialTheme.typography.subtitle2,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )

    // Description
    Text(
        text = stringResource(R.string.about_never_send_collected_app_info),
        style = MaterialTheme.typography.body2,
        modifier = Modifier
            .padding(16.dp)
            .align(Alignment.CenterHorizontally)
            .border(1.dp, MaterialTheme.colors.onBackground, MaterialTheme.shapes.medium)
            .padding(8.dp)
    )

    // Open app info
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            val intent = Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }
    ) {
        Text(text = stringResource(R.string.about_app_info))
    }

    // Open open-source info
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
        }
    ) {
        Text(text = stringResource(R.string.about_open_source))
    }

    // Open store
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            val packageName = context.packageName
            try {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$packageName"))
                )
            } catch (e: ActivityNotFoundException) {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                )
            }
        }
    ) {
        Text(text = stringResource(R.string.about_store))
    }

    // Send email to developer
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "plain/Text"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("jujin@jujinkim.com"))
                putExtra(Intent.EXTRA_SUBJECT, "<${context.getString(R.string.app_name_only)}>")
                type = "message/rfc822"
            }
            context.startActivity(intent)
        }
    ) {
        Text(text = stringResource(R.string.about_contact))
    }

    // Open developer homepage
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            val url = "http://cozelsil.com"
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    ) {
        Text(text = stringResource(R.string.about_homepage))
    }

    // Open GitHub repository
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            val url = "https://github.com/jujinkim/Frequaw-archive"
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_github),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(text = stringResource(R.string.about_github_repo))
        }
    }

    // Suggest issue on github
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            val url = "https://github.com/jujinkim/Frequaw-issue/issues/new/choose"
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    ) {
        Text(text = stringResource(R.string.about_issue_suggestion))
    }

    // Special thanks
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            AlertDialog.Builder(context)
                .setMessage(R.string.about_special_thanks_desc)
                .setNeutralButton(R.string.close, null)
                .create()
                .show()
        }
    ) {
        Text(text = stringResource(R.string.about_special_thanks))
    }
}
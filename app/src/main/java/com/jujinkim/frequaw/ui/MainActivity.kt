package com.jujinkim.frequaw.ui

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import com.google.android.play.core.review.ReviewManagerFactory
import com.jujinkim.frequaw.*
import com.jujinkim.frequaw.data.FrequawDataHelper
import com.jujinkim.frequaw.databinding.SettingsActivityBinding
import com.jujinkim.frequaw.theme.FrequawTheme
import com.jujinkim.frequaw.viewmodel.SettingViewModel
import com.jujinkim.frequaw.viewmodel.WidgetModel
import com.jujinkim.frequaw.widget.FrequawWidget
import com.jujinkim.frequaw.widget.FrequawWidgetUtils
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : AppCompatActivity(), FinishActivityListener {

    lateinit var widgetPreviewLayout: LinearLayout

    private var permissionCheckDialog: AlertDialog? = null

    private var isFirstRun = SharedPref.getAndUpdateFirstRun()
    private var tutorialIntentLauncher: ActivityResultLauncher<Intent>? = null

    lateinit var binding: SettingsActivityBinding
    private val viewModel by lazy {
        ViewModelProvider(this)[SettingViewModel::class.java].apply {
            finishActivityListener = this@MainActivity
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.settings_activity)

        // viewModel
        with(viewModel) {
            widgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )

            if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                settingWidgetStr.value = getString(R.string.default_setting)
                settingContextMenuVisibility.value = View.GONE
            } else {
                settingWidgetStr.value = getString(R.string.widget_pd, widgetId)
                settingContextMenuVisibility.value = View.VISIBLE
            }

            updateWidgetPreviewEvent.observe(this@MainActivity) {
                updateWidgetPreview()
            }
            openWidgetSettingMenuEvent.observe(this@MainActivity) {
                openContextMenu(binding.lytWidgetSettingMenu)
            }
            exportImportHelper = ExportImportHelper(this@MainActivity)

            currentScreen.onEach { screen ->
                when (screen) {
                    Screen.Visual -> {
                        if (FrequawDataHelper.loadWidgetSetting(widgetId).isAdvancedWidgetLayout) {
                            widgetPreviewVisibility.value = View.VISIBLE
                            updateWidgetPreview()
                        }
                    }
                    else -> {
                        widgetPreviewVisibility.value = View.GONE
                    }
                }
            }.launchIn(lifecycleScope)
        }

        with(binding) {
            viewModel = this@MainActivity.viewModel
            lifecycleOwner = this@MainActivity
        }

        // toolBar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.run {
            setDisplayShowCustomEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }

        launchFirstRunTutorial()

        widgetPreviewLayout = findViewById(R.id.settings_widget_preview)

        requestReviewIfInCondition()

        // Set composeView
        binding.settingsComposeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@MainActivity))
            setContent {
                FrequawTheme {
                    SettingsComposable(FrequawDataHelper.load(), viewModel.widgetId)
                }
            }
        }

        if (viewModel.widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            registerForContextMenu(binding.lytWidgetSettingMenu)
        }

        onBackPressedDispatcher.addCallback { viewModel.navigateUp() }

        setResultOfWidget()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                viewModel.navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)

        when (v?.id) {
            R.id.lyt_widgetSettingMenu -> {
                menuInflater.inflate(R.menu.widget_setting_menu, menu)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.widget_setting_menu_open_default_setting -> {
                viewModel.openDefaultSetting(intent)
            }
            R.id.widget_setting_menu_reset_to_default_setting -> {
                viewModel.resetToDefaultSetting(this, intent)
            }
            R.id.widget_setting_menu_erase_this_widget_setting -> {
                viewModel.eraseThisWidgetSetting(this, intent)
            }
        }

        return super.onContextItemSelected(item)
    }

    override fun onResume() {
        super.onResume()

        // Dismiss any permission check dialog if it is shown
        if (permissionCheckDialog != null) {
            permissionCheckDialog?.dismiss()
            permissionCheckDialog = null
        }

        // Message bar layout
        viewModel.updateMessageBarLayoutByChangingMode(
            FrequawDataHelper.loadWidgetSetting(viewModel.widgetId).sortAppBy
        )
        viewModel.checkBatteryOptimizationState()

        // Widget preview layout
        if (FrequawDataHelper.loadWidgetSetting(viewModel.widgetId).isAdvancedWidgetLayout &&
            viewModel.currentScreen.value == Screen.Visual
        ) {
            viewModel.widgetPreviewVisibility.value = View.VISIBLE
            updateWidgetPreview()
        } else {
            viewModel.widgetPreviewVisibility.value = View.GONE
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent

        finish()
        startActivity(intent)
    }

    override fun onStop() {
        Utils.sendUpdateWidgetBr(applicationContext)
        super.onStop()
    }

    private fun requestReviewIfInCondition() {
        val isGoodToRequestReview = SharedPref.getAndUpdateReviewRequest()
        if (!isGoodToRequestReview) return

        val manager = ReviewManagerFactory.create(this)
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(FrequawApp.TAG_DEBUG, "Start review flow")
                manager.launchReviewFlow(this, task.result)
            } else {
                Log.d(FrequawApp.TAG_DEBUG, "Fail to request review, ${task.exception?.message}")
                task.exception?.printStackTrace()
            }
        }
    }

    private fun launchFirstRunTutorial() {
        if (isFirstRun) {
            tutorialIntentLauncher = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == RESULT_OK) {
                    viewModel.navigate(Screen.AppListSortMode)
                    tutorialIntentLauncher?.unregister()
                }
            }
            tutorialIntentLauncher?.launch(Intent(this, TutorialActivity::class.java))
        }
    }

    fun updateWidgetPreview() {
        if (viewModel.widgetPreviewVisibility.value != View.VISIBLE) return

        val widgetModel =
            WidgetModel(viewModel.widgetId).apply { initializeData(this@MainActivity) }
        val isPortrait =
            resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        // Get width/height of the current widget
        val width: Int
        val height: Int

        if (isPortrait) {
            width = FrequawWidget.updatedMinWidths.get(viewModel.widgetId, 0)
            height = FrequawWidget.updatedMaxHeights.get(viewModel.widgetId, 0)
        } else {
            width = FrequawWidget.updatedMaxWidths.get(viewModel.widgetId, 0)
            height = FrequawWidget.updatedMinHeights.get(viewModel.widgetId, 0)
        }

        widgetPreviewLayout.apply {
            layoutParams.apply {
                this.width = width
                this.height = height
            }
            pivotX = width.toFloat()
            pivotY = 0f

            // Update by viewModel

            // col/row count
            val colCount = FrequawWidgetUtils.calcColumnCount(
                width,
                widgetModel.widgetHorizontalPadding,
                widgetModel.iconAreaWidth,
                widgetModel.iconHorizontalGap
            )
            val rowCount = FrequawWidgetUtils.calcRowCount(
                height,
                widgetModel.widgetVerticalPadding,
                widgetModel.iconAreaHeight,
                widgetModel.iconVerticalGap,
                widgetModel.titleVisibility == View.GONE
            )

            // get padding difference between actual padding and min padding
            val horizontalPaddingDifference =
                FrequawWidgetUtils.calcWidgetActualHorizontalPadding(
                    width,
                    colCount,
                    widgetModel.iconAreaWidth,
                    widgetModel.iconHorizontalGap,
                    widgetModel.widgetHorizontalPadding
                ) - widgetModel.widgetHorizontalPadding
            val verticalPaddingDifference =
                FrequawWidgetUtils.calcWidgetActualVerticalPadding(
                    height,
                    rowCount,
                    widgetModel.iconAreaHeight,
                    widgetModel.iconVerticalGap,
                    widgetModel.widgetVerticalPadding,
                    widgetModel.titleVisibility == View.GONE
                ) - widgetModel.widgetVerticalPadding

            // app icon preview
            findViewById<LinearLayout>(R.id.settings_widget_preview_list)?.let { appAreaPreview ->
                appAreaPreview.setPadding(
                    horizontalPaddingDifference,
                    if (widgetModel.titleVisibility == View.GONE) verticalPaddingDifference else 0,
                    horizontalPaddingDifference,
                    verticalPaddingDifference
                )
                appAreaPreview.removeAllViews()

                repeat(rowCount) {
                    val rowLayout = LinearLayout(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                    }

                    repeat(colCount) {
                        val iconArea = LinearLayout(context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                1f
                            )
                            setBackgroundColor(resources.getColor(R.color.preview_icon_area))
                            setPadding(
                                widgetModel.iconHorizontalGap,
                                widgetModel.iconVerticalGap,
                                widgetModel.iconHorizontalGap,
                                widgetModel.iconVerticalGap
                            )
                        }.apply {
                            val icon = View(context).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT
                                )
                                setBackgroundColor(resources.getColor(R.color.preview_icon))
                            }
                            addView(icon)
                        }

                        rowLayout.addView(iconArea)
                    }
                    appAreaPreview.addView(rowLayout)
                }
            }

            // widget padding
            setPadding(
                widgetModel.widgetHorizontalPadding,
                if (widgetModel.titleVisibility == View.GONE) widgetModel.widgetVerticalPadding else 0,
                widgetModel.widgetHorizontalPadding,
                widgetModel.widgetVerticalPadding
            )

            // title bar preview
            findViewById<TextView>(R.id.settings_widget_preview_title)?.visibility =
                widgetModel.titleVisibility
        }
    }

    override fun onDestroy() {
        if (viewModel.widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            unregisterForContextMenu(binding.lytWidgetSettingMenu)
        }

        super.onDestroy()
    }

    private fun setResultOfWidget() {
        val widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // Do nothing if the AppWidget_Id is invalid.
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            return
        }

        val resultIntent = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }

        setResult(RESULT_OK, resultIntent)

        // Finish activity if this is "Adding widget" flow
        if (!SharedPref.getAddedWidget(widgetId)) {
            SharedPref.setAddedWidget(true, widgetId)
            Toast.makeText(
                this,
                String.format(getString(R.string.add_new_widget_pd), widgetId),
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    override fun onActivityFinishRequested() {
        finish()
    }
}

interface FinishActivityListener {
    fun onActivityFinishRequested()
}
package com.jujinkim.frequaw

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import com.jujinkim.frequaw.data.FrequawDataHelper
import com.jujinkim.frequaw.data.FrequawWidgetSettingData
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.math.min
import kotlin.math.roundToInt

class IconHelper(context: Context, private val widgetId: Int) {
    private val widgetSetting: FrequawWidgetSettingData
    get() = FrequawDataHelper.loadWidgetSetting(widgetId)

    private var layerSize: Int
    private var iconSize: Int

    private var packageManager: PackageManager = context.packageManager
    private var cornerRadius: Float

    private val isForceApplyShape = widgetSetting.isForceIconShapeClip

    private val iconPack: IconPack

    init {
        layerSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, APP_ICON_SIZE_DP, context.resources.displayMetrics
        ).roundToInt()
        iconSize =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                (layerSize / (1 + 2 * AdaptiveIconDrawable.getExtraInsetFraction())).toInt()
            else
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 72f, context.resources.displayMetrics
                ).roundToInt()
        cornerRadius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, SQUARE_CORNER_RADIUS_DP, context.resources.displayMetrics
        )

        iconPack = IconPack(widgetSetting.appIconPackPackage, packageManager)
    }

    @Throws(NameNotFoundException::class)
    fun getAppIcon(packageName: String, type: AppIconStyle = AppIconStyle.System): Bitmap {
        try {
            val drawable = if (iconPack.isReady) {
                iconPack.getAppIconDrawable(packageName) ?:
                iconPack.applyIconPackTheme(packageManager.getApplicationIcon(packageName))
            } else {
                packageManager.getApplicationIcon(packageName)
            }

            return getClippedIcon(drawable, type)

        } catch (e: NameNotFoundException) {
            e.printStackTrace()
            throw NameNotFoundException()
        }
    }

    fun getClippedIcon(baseIcon: Drawable, iconType: AppIconStyle) : Bitmap {
        val iconBitmap = if (baseIcon is BitmapDrawable) {
            // old icons
            if (iconType != AppIconStyle.System && isForceApplyShape) {
                getBitmapClipped(baseIcon.toBitmap(config = Bitmap.Config.ARGB_4444), iconType)
            } else {
                baseIcon.toBitmap(config = Bitmap.Config.ARGB_4444)
            }
        } else if (iconType == AppIconStyle.System || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            baseIcon.toBitmap(config = Bitmap.Config.ARGB_4444)
        } else if (baseIcon is AdaptiveIconDrawable) {
            val drr = arrayOfNulls<Drawable>(2)
            drr[0] = baseIcon.background
            drr[1] = baseIcon.foreground
            val layerDrawable = LayerDrawable(drr).apply {
                setLayerGravity(0, Gravity.CENTER)
                setLayerGravity(1, Gravity.CENTER)
                setLayerSize(0, layerSize, layerSize)
                setLayerSize(1, layerSize, layerSize)
                //setLayerInset(1, 0, 0, 0, 0)
            }
            val bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_4444)
            val canvas = Canvas(bitmap)
            layerDrawable.setBounds(0, 0, iconSize, iconSize)
            layerDrawable.draw(canvas)
            getBitmapClipped(bitmap, iconType)
        } else {
            if (iconType != AppIconStyle.System && isForceApplyShape) {
                getBitmapClipped(baseIcon.toBitmap(config = Bitmap.Config.ARGB_4444), iconType)
            } else {
                baseIcon.toBitmap(config = Bitmap.Config.ARGB_4444)
            }
        }

        return iconBitmap
    }

    private fun getBitmapClipped(bitmap: Bitmap, iconType: AppIconStyle) : Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444).apply { density = bitmap.density }
        val path = when (iconType) {
            AppIconStyle.Circle-> circlePath(width, height)
            AppIconStyle.Square -> squarePath(width, height)
            AppIconStyle.Squircle -> squirclePath(width, height)
            AppIconStyle.RoundedSquare -> roundSquarePath(width, height)
            else -> circlePath(width, height)
        }

        val canvas = Canvas(outputBitmap)
        canvas.save()
        canvas.clipPath(path)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.restore()
        bitmap.recycle()
        return outputBitmap
    }

    private fun circlePath(width: Int, height: Int) =
        Path().apply {
            addCircle(
                width * .5f,
                height * .5f,
                min(width.toFloat(), height * .5f),
                Path.Direction.CCW
            )
        }

    private fun squarePath(width: Int, height: Int) =
        Path().apply {
            addRect(0f, 0f, width.toFloat(), height.toFloat(), Path.Direction.CCW)
        }

    private fun roundSquarePath(width: Int, height: Int) =
        Path().apply {
            addRoundRect(0f, 0f, width.toFloat(), height.toFloat(), cornerRadius, cornerRadius, Path.Direction.CCW)
        }

    private fun squirclePath(width: Int, height: Int) =
        Path().apply {
            // 4 Cubic bezier curves
            moveTo(0f, height * 0.5f)
            cubicTo(0f, height * SQUIRCLE_FACTOR, width * SQUIRCLE_FACTOR, 0f, width * 0.5f, 0f)  // left top
            cubicTo(width * (1 - SQUIRCLE_FACTOR), 0f, width.toFloat(), height * SQUIRCLE_FACTOR, width.toFloat(), height * 0.5f)   // right top
            cubicTo(width.toFloat(), height * (1 - SQUIRCLE_FACTOR), width * (1 - SQUIRCLE_FACTOR), height.toFloat(), width * 0.5f, height.toFloat()) // right bottom
            cubicTo(width * SQUIRCLE_FACTOR, height.toFloat(), 0f, height * (1 - SQUIRCLE_FACTOR), 0f, height * 0.5f)   // left bottom
            close()
        }

    companion object {
        const val APP_ICON_SIZE_DP = 108f
        const val SQUARE_CORNER_RADIUS_DP = 8f
        const val SQUIRCLE_FACTOR = 0.125f
    }
}

class IconPack(
    val packageName: String,
    val packageManager: PackageManager
) {
    var isReady = false

    private lateinit var iconPackRes: Resources
    private var iconPackResMap = hashMapOf<String, String>()

    private var commonBackImages: MutableList<Bitmap> = mutableListOf()
    private var commonMask: Bitmap? = null
    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT) }
    private var commonFrontImage: Bitmap? = null
    private var commonFactor = 1f

    init {
        if (packageName.isNotBlank()) initIconPack(packageName, packageManager)
    }

    private fun initIconPack(packageName: String,
                             packageManager: PackageManager) {

        // Get XmlPullParser from app filter xml
        val xmlPullParser: XmlPullParser
        try {
            iconPackRes = packageManager.getResourcesForApplication(packageName)
            val appFilterId = iconPackRes.getIdentifier("appfilter", "xml", packageName)
            if (appFilterId > 0) {
                xmlPullParser = iconPackRes.getXml(appFilterId)
            } else {
                val appFilterStream: InputStream = iconPackRes.getAssets().open("appfilter.xml")

                val factory = XmlPullParserFactory.newInstance()
                factory.isNamespaceAware = true
                xmlPullParser = factory.newPullParser()
                xmlPullParser.setInput(appFilterStream, "utf-8")
            }
        } catch (e: IOException) {
            Log.e(FrequawApp.TAG_DEBUG, "Error while load icon pack: no appfilter.xml file")
            e.printStackTrace()
            return
        } catch (e: NameNotFoundException) {
            Log.e(FrequawApp.TAG_DEBUG, "Error while load icon pack: package name's icon pack is not found")
            e.printStackTrace()
            return
        }

        // load icon drawable resource strings
        var eventType = xmlPullParser.eventType
        do {
            if (eventType != XmlPullParser.START_TAG) {
                eventType = xmlPullParser.next()
                continue
            }

            // icon pack bg/fg/mask/factor
            when (xmlPullParser.name.lowercase(Locale.getDefault())) {
                "iconback" -> {
                    for (i in 0 until xmlPullParser.attributeCount) {
                        if (xmlPullParser.getAttributeName(0).equals("img1")) {
                            getDrawableFromName(xmlPullParser.getAttributeValue(i))?.let {
                                commonBackImages.add(it.toBitmap(config = Bitmap.Config.ARGB_4444))
                            }
                        }
                    }
                    eventType = xmlPullParser.next()
                    continue
                }
                "iconmask" -> {
                    if (xmlPullParser.attributeCount > 0 &&
                        xmlPullParser.getAttributeName(0).equals("img1")) {
                        val drawableName = xmlPullParser.getAttributeValue(0)
                        commonMask = getDrawableFromName(drawableName)?.toBitmap(config = Bitmap.Config.ARGB_4444)
                    }
                    eventType = xmlPullParser.next()
                    continue
                }
                "iconupon" -> {
                    if (xmlPullParser.attributeCount > 0 &&
                        xmlPullParser.getAttributeName(0).equals("img1")) {
                        val drawableName = xmlPullParser.getAttributeValue(0)
                        commonFrontImage = getDrawableFromName(drawableName)?.toBitmap(config = Bitmap.Config.ARGB_4444)
                    }
                    eventType = xmlPullParser.next()
                    continue
                }
                "scale" -> {
                    if (xmlPullParser.attributeCount > 0 &&
                        xmlPullParser.getAttributeName(0).equals("factor")) {
                        commonFactor = xmlPullParser.getAttributeValue(0).toFloat()
                    }
                    eventType = xmlPullParser.next()
                    continue
                }
            }

            if (xmlPullParser.name.lowercase(Locale.getDefault()) != "item") {
                eventType = xmlPullParser.next()
                continue
            }

            val componentStr = xmlPullParser.getAttributeValue(null, "component")
            val drawableStr = xmlPullParser.getAttributeValue(null, "drawable")
            if (componentStr.isNullOrBlank() || drawableStr.isNullOrBlank()) {
                eventType = xmlPullParser.next()
                continue
            }

            iconPackResMap[componentStr] = drawableStr
            Log.d(FrequawApp.TAG_DEBUG, "icon pack loaded - $componentStr + $drawableStr")

            eventType = xmlPullParser.next()
        } while (eventType != XmlPullParser.END_DOCUMENT)

        isReady = true
    }

    fun getAppIconDrawable(packageName: String) : Drawable? {
        if (!isReady) return null

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val componentName = launchIntent?.let { it.component.toString() } ?: return null

        var resStr = iconPackResMap[componentName]
        if (resStr.isNullOrBlank()) {
            val start = componentName.indexOf("{") + 1
            val end = componentName.indexOf("}", start)
            if (end > start) {
                resStr = componentName.substring(start, end)
                    .lowercase(Locale.getDefault())
                    .replace("/", "_")
                    .replace(".", "_")
            }
        }

        val id = iconPackRes.getIdentifier(resStr, "drawable", this.packageName)
        if (id == 0) return null

        return ResourcesCompat.getDrawable(iconPackRes, id, null)
    }

    private fun getDrawableFromName(name: String) : Drawable? {
        if (name.isBlank()) return null
        val id = iconPackRes.getIdentifier(name, "drawable", this.packageName)
        if (id == 0) return null
        return ResourcesCompat.getDrawable(iconPackRes, id, null)
    }

    fun applyIconPackTheme(drawable: Drawable) : Drawable {
        if (commonBackImages.isEmpty()) {
            return drawable
        }

        val backBitmap = commonBackImages.random()
        val w = backBitmap.width
        val h = backBitmap.height
        val outputBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(outputBitmap).apply { drawBitmap(backBitmap, 0f, 0f, null) }

        // app image
        canvas.drawBitmap(drawable.toBitmap(w, h, Bitmap.Config.ARGB_4444), 0f, 0f, null)

        // mask
        commonMask?.let { mask ->
            canvas.drawBitmap(mask, 0f, 0f, maskPaint)
        }

        // front(upon)
        commonFrontImage?.let { front ->
            canvas.drawBitmap(front, 0f, 0f, null)
        }

        return outputBitmap.toDrawable(FrequawApp.appContext.resources)
    }
}
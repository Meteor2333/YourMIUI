package cc.meteormc.yourmiui.xposed.systemui.feature

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.withScale
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Option
import cc.meteormc.yourmiui.common.Option.Type
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.findArg
import cc.meteormc.yourmiui.xposed.operator
import kotlinx.coroutines.channels.Channel
import kotlin.math.sqrt

object FixSplashScreen : Feature(
    key = "fix_splash_screen",
    nameRes = R.string.feature_systemui_fix_splash_screen_name,
    descriptionRes = R.string.feature_systemui_fix_splash_screen_description,
    testEnvironmentRes = R.string.feature_systemui_fix_splash_screen_test_environment
) {
    private const val ALLOW_LAUNCH_PACKAGE = "com.miui.home"

    private var replaceBackgroundColor: Boolean = true

    private val infoCache = mutableMapOf<String, SplashScreenInfo>()
    private var realIconMapping = mapOf(
        "com.android.contacts" to "com.android.contacts.activities.PeopleActivity" to "com.android.phone"
    )

    private data class SplashScreenInfo(
        val isDark: Boolean,
        val icon: Drawable,
        val background: Int
    )

    override fun onLoadPackage() {
        val swiClass = operator("android.window.StartingWindowInfo") ?: return
        // name: targetActivityInfo | type: android.content.pm.ActivityInfo
        val targetActivityInfoField = swiClass.field("targetActivityInfo") ?: return
        // name: taskInfo | type: android.app.ActivityManager$RunningTaskInfo
        val taskInfoField = swiClass.field("taskInfo") ?: return
        // name: mlaunchPackageName | type: java.lang.String
        val launchPackageNameField = swiClass.field("mlaunchPackageName") ?: return

        val rtiClass = operator($$"android.app.ActivityManager$RunningTaskInfo") ?: return
        // name: topActivityInfo | type: android.content.pm.ActivityInfo
        val topActivityInfoField = rtiClass.field("topActivityInfo") ?: return

        val sswaClass = operator($$"com.android.wm.shell.startingsurface.SplashscreenContentDrawer$SplashScreenWindowAttrs") ?: return
        // name: mIconBgColor | type: int
        val iconBgColorField = sswaClass.field("mIconBgColor") ?: return
        // name: mSplashScreenIcon | type: android.graphics.drawable.Drawable
        val splashScreenIconField = sswaClass.field("mSplashScreenIcon") ?: return
        // name: mWindowBgColor | type: int
        val windowBgColorField = sswaClass.field("mWindowBgColor") ?: return

        val iconSizeChannel = Channel<Int?>(Channel.BUFFERED)
        val iconDefaultSizeChannel = Channel<Int?>(Channel.BUFFERED)
        val activityInfoChannel = Channel<ActivityInfo?>(Channel.BUFFERED)
        fun ActivityInfo.loadSplashScreenInfo(context: Context, iconSize: Int, iconDefaultSize: Int): SplashScreenInfo {
            val configuration = context.resources.configuration
            val isDark = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

            val pm = context.packageManager
            val icon = pm.getApplicationIcon(realIconMapping[packageName to targetActivity] ?: packageName)
            val info = infoCache.getOrPut(packageName) {
                SplashScreenInfo(
                    isDark,
                    icon.normalizeIcon(context, iconSize, iconDefaultSize),
                    icon.getThemeColor(isDark)
                )
            }

            if (info.isDark != isDark) {
                infoCache.remove(packageName)
                return loadSplashScreenInfo(context, iconSize, iconDefaultSize)
            }

            return info
        }

        operator("com.android.wm.shell.startingsurface.SplashscreenContentDrawer") {
            // name: mIconSize | type: int
            val iconSizeField = field("mIconSize") ?: return@operator
            // name: mDefaultIconSize | type: int
            val iconDefaultSizeField = field("mDefaultIconSize") ?: return@operator
            // modifier: public final | signature: updateDensity()V
            val updateDensityMethod = method("updateDensity") ?: return@operator
            method("makeSplashScreenContentView")?.hookBefore {
                val swi = it.findArg(swiClass.delegate) ?: return@hookBefore
                if (launchPackageNameField.get<String>(swi) != ALLOW_LAUNCH_PACKAGE) return@hookBefore

                updateDensityMethod.call(it.thisObject)
                iconSizeChannel.trySend(iconSizeField[it.thisObject])
                iconDefaultSizeChannel.trySend(iconDefaultSizeField[it.thisObject])
                activityInfoChannel.trySend(targetActivityInfoField[swi] ?: topActivityInfoField[taskInfoField[swi]])
            }

            // modifier: public static | signature: getWindowAttrs(Landroid/content/Context;Lcom/android/wm/shell/startingsurface/SplashscreenContentDrawer$SplashScreenWindowAttrs;)V
            method("getWindowAttrs")?.hookAfter {
                val currentIconSize = iconSizeChannel.tryReceive().getOrNull() ?: return@hookAfter
                val currentIconDefaultSize = iconDefaultSizeChannel.tryReceive().getOrNull() ?: return@hookAfter
                val currentActivityInfo = activityInfoChannel.tryReceive().getOrNull() ?: return@hookAfter
                val context = it.findArg(Context::class.java) ?: return@hookAfter
                val attrs = it.findArg(sswaClass.delegate) ?: return@hookAfter
                val appData by lazy {
                    currentActivityInfo.loadSplashScreenInfo(context, currentIconSize, currentIconDefaultSize)
                }

                if (iconBgColorField.get<Int>(attrs) == 0) {
                    iconBgColorField[attrs] = 0x01000000
                }

                if (splashScreenIconField.get<Drawable>(attrs) == null) {
                    splashScreenIconField[attrs] = appData.icon
                }

                if (replaceBackgroundColor && windowBgColorField.get<Int>(attrs) == 0) {
                    windowBgColorField[attrs] = appData.background
                }
            }
        }
    }

    override fun getOptions(): List<Option<*>> {
        return listOf(
            Option(
                "replace_background_color",
                R.string.option_systemui_fix_splash_screen_replace_background_color_name,
                R.string.option_systemui_fix_splash_screen_replace_background_color_summary,
                Type.Switch(),
                replaceBackgroundColor
            ) { replaceBackgroundColor = it }
        )
    }

    private fun Bitmap.getPixels(stride: Int): IntArray {
        val pixels = IntArray(width * height)
        getPixels(pixels, 0, stride, 0, 0, width, height)
        return pixels
    }

    private fun Drawable.toBitmap(): Bitmap {
        if (this is BitmapDrawable) return this.bitmap

        val width = intrinsicWidth.takeIf { it > 0 } ?: 32
        val height = intrinsicHeight.takeIf { it > 0 } ?: 32
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, width, height)
        draw(canvas)
        return bitmap
    }

    private const val MIN_ALPHA = 40
    private const val TARGET_FILL_RATIO = 0.65f
    private fun Drawable.normalizeIcon(context: Context, iconSize: Int, iconDefaultSize: Int): Drawable {
        // AdaptiveIconDrawable会进行自适应 无需手动调整
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this is AdaptiveIconDrawable) return this

        val bitmap = createBitmap(iconSize, iconSize)
        val pixels = bitmap.getPixels(iconSize)

        var minX = iconSize
        var maxX = -1
        var minY = iconSize
        var maxY = -1
        var visiblePixels = 0
        for (y in 0 until iconSize) {
            for (x in 0 until iconSize) {
                if ((pixels[y * iconSize + x] shr 24 and 0xFF) > MIN_ALPHA) {
                    visiblePixels++
                    minX = minOf(minX, x)
                    maxX = maxOf(maxX, x)
                    minY = minOf(minY, y)
                    maxY = maxOf(maxY, y)
                }
            }
        }

        // 按比例计算需要缩放的大小
        val contentWidth = maxX - minX + 1
        val contentHeight = maxY - minY + 1
        val contentArea = contentWidth * contentHeight
        val currentFillRatio = contentArea.toFloat() / (iconSize * iconSize)
        var scale = if (visiblePixels != 0 && currentFillRatio > TARGET_FILL_RATIO) {
            sqrt(TARGET_FILL_RATIO / currentFillRatio)
        } else {
            1f
        }

        // 在com.android.wm.shell.startingsurface.SplashscreenIconDrawableFactory$ImmobileIconDrawable这个类的构造器中
        // 有一段意义不明的缩放逻辑 执行缩放的值为iconSize/iconDefaultSize
        // 为了保证不出现图标显示不全或黑边的问题 需要将这额外的值除掉
        if (iconDefaultSize != 0) {
            scale /= iconSize.toFloat() / iconDefaultSize
        }

        // 保证居中地缩放绘制
        val canvas = Canvas(bitmap)
        val oldBounds = Rect(bounds)
        setBounds(0, 0, iconSize, iconSize)
        canvas.withScale(scale, scale, iconSize / 2f, iconSize / 2f) {
            draw(this)
        }
        bounds = oldBounds

        val normalized = bitmap.toDrawable(context.resources)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AdaptiveIconDrawable(normalized, null)
        } else {
            normalized
        }
    }

    private fun Drawable.getThemeColor(isDark: Boolean): Int {
        fun Bitmap.isMonoColor(): Boolean {
            var total = 0
            var lowSat = 0
            var highSat = 0
            val hsv = FloatArray(3)
            for (color in this.getPixels(width)) {
                Color.colorToHSV(color, hsv)
                val saturation = hsv[1]

                total++
                if (saturation < 0.2f) lowSat++
                if (saturation > 0.35f) highSat++
            }

            if (total == 0) return true
            val lowRatio = lowSat.toFloat() / total
            val highRatio = highSat.toFloat() / total
            return lowRatio > 0.75f && highRatio < 0.1f
        }

        val source = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this is AdaptiveIconDrawable) {
            val background = background.toBitmap()
            val backgroundIsMono = background.isMonoColor()
            val foreground = foreground.toBitmap()
            val foregroundIsMono = foreground.isMonoColor()

            if (foregroundIsMono && !backgroundIsMono) {
                background
            } else if (backgroundIsMono && !foregroundIsMono) {
                foreground
            } else this.toBitmap()
        } else this.toBitmap()

        val frequencys = mutableMapOf<Int, Int>()
        val pixels = source.getPixels(source.width)
        for (color in pixels) {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            if (hsv[1] < 0.08f) continue

            val r = (Color.red(color) / 32) * 32
            val g = (Color.green(color) / 32) * 32
            val b = (Color.blue(color) / 32) * 32
            val key = Color.rgb(r, g, b)
            frequencys[key] = (frequencys[key] ?: 0) + 1
        }

        var color = frequencys.maxByOrNull { it.value }?.key ?: return Color.TRANSPARENT
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        var h = hsv[0]
        var s = hsv[1]
        var v = hsv[2]
        val ratio: Float

        h = h.coerceIn(5f, 354f)
        if (s > 0.5f) s -= (s - 0.5f) * 0.6f
        if (!isDark) {
            ratio = 0.6f
        } else {
            ratio = 0.75f
            if (v > 0.7f) v -= (v - 0.7f) * 2.5f
        }
        color = Color.HSVToColor(floatArrayOf(h, s, v))

        val inv = 1f - ratio
        val overlay = if (isDark) Color.BLACK else Color.WHITE
        color = Color.rgb(
            (Color.red(color) * ratio + Color.red(overlay) * inv).toInt(),
            (Color.green(color) * ratio + Color.green(overlay) * inv).toInt(),
            (Color.blue(color) * ratio + Color.blue(overlay) * inv).toInt()
        )

        return color
    }
}
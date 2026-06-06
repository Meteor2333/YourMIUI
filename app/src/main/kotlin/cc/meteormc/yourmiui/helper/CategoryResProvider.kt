package cc.meteormc.yourmiui.helper

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.YourMIUI
import cc.meteormc.yourmiui.api.Category

object CategoryResProvider {
    private val iconCache = mutableMapOf<Category, Drawable>()
    private val displayNameCache = mutableMapOf<Category, String>()

    fun Category.getIcon(): Drawable {
        val context = YourMIUI.get()

        fun provideByPkg(packageName: String): Drawable {
            val pm = context.packageManager
            return pm.getApplicationIcon(packageName)
        }

        fun provideByRes(@DrawableRes resId: Int): Drawable {
            return AppCompatResources.getDrawable(context, resId)!!
        }

        return iconCache[this] ?: when (this) {
            Category.SYSTEM -> provideByPkg("android")
            Category.UI -> provideByPkg("com.android.systemui")
            Category.DESKTOP -> provideByPkg("com.miui.home")
            Category.MARTET -> provideByPkg("com.xiaomi.market")
            Category.MIUI_PLUS -> provideByPkg("com.xiaomi.mirror")
            Category.MMS -> provideByPkg("com.android.mms")
            Category.NFC -> provideByPkg("com.android.nfc")
            Category.NOTIFICATION -> provideByPkg("com.miui.notification")
            Category.PACKAGE_INSTALLER -> provideByPkg("com.miui.packageinstaller")
            Category.PORTAL -> provideByPkg("com.miui.contentextension")
            Category.SECURITY_CENTER -> provideByPkg("com.miui.securitycenter")
            Category.SETTINGS -> provideByPkg("com.android.settings")
            Category.WALLPAPER -> provideByPkg("com.miui.miwallpaper")
        }.also { iconCache[this] = it }
    }

    fun Category.getDiaplayName(): String {
        val context = YourMIUI.get()

        fun provideByPkg(packageName: String): String {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            return pm.getApplicationLabel(info).toString()
        }

        fun provideByRes(@StringRes resId: Int): String {
            return context.getString(resId)
        }

        return displayNameCache[this] ?: when (this) {
            Category.SYSTEM -> provideByRes(R.string.category_system)
            Category.UI -> provideByRes(R.string.category_ui)
            Category.DESKTOP -> provideByRes(R.string.category_desktop)
            Category.MARTET -> provideByPkg("com.xiaomi.market")
            Category.MIUI_PLUS -> provideByRes(R.string.category_miuiplus)
            Category.MMS -> provideByPkg("com.android.mms")
            Category.NFC -> provideByRes(R.string.category_nfc)
            Category.NOTIFICATION -> provideByRes(R.string.category_notification)
            Category.PACKAGE_INSTALLER -> provideByRes(R.string.category_packageinstaller)
            Category.PORTAL -> provideByPkg("com.miui.contentextension")
            Category.SECURITY_CENTER -> provideByPkg("com.miui.securitycenter")
            Category.SETTINGS -> provideByPkg("com.android.settings")
            Category.WALLPAPER -> provideByPkg("com.miui.miwallpaper")
        }.also { displayNameCache[this] = it }
    }
}
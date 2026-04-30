package cc.meteormc.yourmiui.xposed.superwallpaper

import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.superwallpaper.feature.DisablePause

object SuperWallpaper : XposedScope(
    "com.miui.miwallpaper.earth" to null,
    "com.miui.miwallpaper.geometry" to null,
    "com.miui.miwallpaper.mars" to null,
    "com.miui.miwallpaper.saturn" to null,
    "com.miui.miwallpaper.snowmountain" to null,
    nameRes = R.string.scope_superwallpaper
) {
    override fun getFeatures(): Iterable<XposedFeature> {
        return listOf(
            DisablePause
        )
    }
}
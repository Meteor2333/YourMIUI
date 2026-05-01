package cc.meteormc.yourmiui.xposed.superwallpaper

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.superwallpaper.feature.DisablePause

object SuperWallpaper : Scope(
    "com.miui.miwallpaper.earth" to null,
    "com.miui.miwallpaper.geometry" to null,
    "com.miui.miwallpaper.mars" to null,
    "com.miui.miwallpaper.saturn" to null,
    "com.miui.miwallpaper.snowmountain" to null,
    nameRes = R.string.scope_superwallpaper
) {
    override fun getFeatures(): List<Feature> {
        return listOf(
            DisablePause
        )
    }
}
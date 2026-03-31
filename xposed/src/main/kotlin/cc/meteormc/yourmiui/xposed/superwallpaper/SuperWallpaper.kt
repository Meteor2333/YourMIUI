package cc.meteormc.yourmiui.xposed.superwallpaper

import cc.meteormc.yourmiui.core.Feature
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.superwallpaper.feature.DisablePause

object SuperWallpaper : XposedScope(
    "com.miui.miwallpaper.earth",
    "com.miui.miwallpaper.geometry",
    "com.miui.miwallpaper.mars",
    "com.miui.miwallpaper.saturn",
    "com.miui.miwallpaper.snowmountain",
    nameRes = R.string.scope_superwallpaper
) {
    override fun getFeatures(): Iterable<Feature> {
        return listOf(
            DisablePause
        )
    }
}
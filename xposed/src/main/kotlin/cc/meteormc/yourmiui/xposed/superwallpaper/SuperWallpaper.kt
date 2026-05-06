package cc.meteormc.yourmiui.xposed.superwallpaper

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.common.data.RestartMethod
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.superwallpaper.feature.DisablePause

object SuperWallpaper : Scope(
    "com.miui.miwallpaper.earth",
    "com.miui.miwallpaper.geometry",
    "com.miui.miwallpaper.mars",
    "com.miui.miwallpaper.saturn",
    "com.miui.miwallpaper.snowmountain",
    nameRes = R.string.scope_superwallpaper
) {
    override fun getFeatures(): List<Feature> {
        return listOf(
            DisablePause
        )
    }

    override fun getRestartMethod() = RestartMethod.DoNothing
}
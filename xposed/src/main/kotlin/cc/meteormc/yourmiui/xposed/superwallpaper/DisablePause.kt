package cc.meteormc.yourmiui.xposed.superwallpaper

import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.xposed.hookResult
import cc.meteormc.yourmiui.xposed.operator

@FeatureRegister(
    Category.SUPER_WALLPAPER,
    "@string/feature_superwallpaper_disable_pause_name",
    "@string/feature_superwallpaper_disable_pause_description",
    "@string/feature_superwallpaper_disable_pause_warning"
)
@RequiredScope("com.miui.miwallpaper.earth")
@RequiredScope("com.miui.miwallpaper.geometry")
@RequiredScope("com.miui.miwallpaper.mars")
@RequiredScope("com.miui.miwallpaper.saturn")
@RequiredScope("com.miui.miwallpaper.snowmountain")
object DisablePause : FeatureHooker {
    override fun hook(packageName: String) {
        operator("com.miui.miwallpaper.basesuperwallpaper.SuperWallpaper") {
            // modifier: protected | signature: getDeskPauseDelay()I
            method("getDeskPauseDelay")?.hookResult(Int.MAX_VALUE)

            // modifier: protected | signature: getWallPaperAod2LockPauseDelay()I
            method("getWallPaperAod2LockPauseDelay")?.hookResult(Int.MAX_VALUE)

            // modifier: protected | signature: getWallPaperOffsetDelay()I
            method("getWallPaperOffsetDelay")?.hookResult(Int.MAX_VALUE)
        }
    }
}
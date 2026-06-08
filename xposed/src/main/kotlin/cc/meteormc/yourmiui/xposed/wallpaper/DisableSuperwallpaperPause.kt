package cc.meteormc.yourmiui.xposed.wallpaper

import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.api.data.HookContext
import cc.meteormc.yourmiui.xposed.hookResult
import cc.meteormc.yourmiui.xposed.reflect

@FeatureRegister(
    Category.WALLPAPER,
    "@string/feature_wallpaper_disable_superwallpaper_pause_name",
    "@string/feature_wallpaper_disable_superwallpaper_pause_description",
    "@string/feature_wallpaper_disable_superwallpaper_pause_warning"
)
@RequiredScope("com.miui.miwallpaper.earth")
@RequiredScope("com.miui.miwallpaper.geometry")
@RequiredScope("com.miui.miwallpaper.mars")
@RequiredScope("com.miui.miwallpaper.saturn")
@RequiredScope("com.miui.miwallpaper.snowmountain")
object DisableSuperwallpaperPause : FeatureHooker {
    override fun hook(context: HookContext) {
        context.reflect("com.miui.miwallpaper.basesuperwallpaper.SuperWallpaper") {
            // modifier: protected | signature: getDeskPauseDelay()I
            method("getDeskPauseDelay")?.hookResult(Int.MAX_VALUE)

            // modifier: protected | signature: getWallPaperAod2LockPauseDelay()I
            method("getWallPaperAod2LockPauseDelay")?.hookResult(Int.MAX_VALUE)

            // modifier: protected | signature: getWallPaperOffsetDelay()I
            method("getWallPaperOffsetDelay")?.hookResult(Int.MAX_VALUE)
        }
    }
}
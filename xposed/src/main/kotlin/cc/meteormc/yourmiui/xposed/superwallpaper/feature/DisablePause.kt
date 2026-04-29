package cc.meteormc.yourmiui.xposed.superwallpaper.feature

import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.operator

object DisablePause : XposedFeature(
    key = "disable_superwallpaper_pause",
    nameRes = R.string.feature_superwallpaper_disable_pause_name,
    descriptionRes = R.string.feature_superwallpaper_disable_pause_description,
    warningRes = R.string.feature_superwallpaper_disable_pause_warning,
    testEnvironmentRes = R.string.feature_superwallpaper_disable_pause_test_environment
) {
    override fun onLoadPackage() {
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
package cc.meteormc.yourmiui.xposed.superwallpaper.feature

import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature

object DisablePause : XposedFeature(
    key = "superwallpaper_disable_pause",
    nameRes = R.string.feature_superwallpaper_disable_pause_name,
    descriptionRes = R.string.feature_superwallpaper_disable_pause_description,
    warningRes = R.string.feature_superwallpaper_disable_pause_warning,
    testEnvironmentRes = R.string.feature_superwallpaper_disable_pause_test_environment
) {
    override fun init() {
        helper("com.miui.miwallpaper.basesuperwallpaper.SuperWallpaper") {
            // modifier: protected | signature: getDeskPauseDelay()I
            method("getDeskPauseDelay")?.hookResult(Int.MAX_VALUE)
            // modifier: protected | signature: getWallPaperAod2LockPauseDelay()I
            method("getWallPaperAod2LockPauseDelay")?.hookResult(Int.MAX_VALUE)
            // modifier: protected | signature: getWallPaperOffsetDelay()I
            method("getWallPaperOffsetDelay")?.hookResult(Int.MAX_VALUE)

            // 不知道是干嘛的 不hook没有影响 hook了也没什么用
//            // modifier: public | signature: sendInitBlockPause()V
//            method("sendInitBlockPause")?.hook(XC_MethodReplacement.DO_NOTHING)
        }
    }
}
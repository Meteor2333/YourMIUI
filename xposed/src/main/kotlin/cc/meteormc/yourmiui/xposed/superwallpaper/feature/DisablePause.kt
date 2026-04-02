package cc.meteormc.yourmiui.xposed.superwallpaper.feature

import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature
import de.robv.android.xposed.XC_MethodReplacement

object DisablePause : XposedFeature(
    key = "superwallpaper_disable_pause",
    nameRes = R.string.feature_superwallpaper_disable_pause_name,
    descriptionRes = R.string.feature_superwallpaper_disable_pause_description,
    warningRes = R.string.feature_superwallpaper_disable_pause_warning,
    testEnvironmentRes = R.string.feature_superwallpaper_disable_pause_test_environment
) {
    override fun init() {
        helper("com.miui.miwallpaper.basesuperwallpaper.SuperWallpaper")?.operate {
            // modifier: protected | signature: getDeskPauseDelay()I
            method("getDeskPauseDelay")?.hook(XC_MethodReplacement.returnConstant(Int.MAX_VALUE))
            // modifier: protected | signature: getWallPaperAod2LockPauseDelay()I
            method("getWallPaperAod2LockPauseDelay")?.hook(XC_MethodReplacement.returnConstant(Int.MAX_VALUE))
            // modifier: protected | signature: getWallPaperOffsetDelay()I
            method("getWallPaperOffsetDelay")?.hook(XC_MethodReplacement.returnConstant(Int.MAX_VALUE))

            // unknown effect
//            // modifier: public | signature: sendInitBlockPause()V
//            method("sendInitBlockPause")?.hook(XC_MethodReplacement.DO_NOTHING)
        }
    }
}
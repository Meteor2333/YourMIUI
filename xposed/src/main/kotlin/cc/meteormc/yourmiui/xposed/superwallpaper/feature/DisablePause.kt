package cc.meteormc.yourmiui.xposed.superwallpaper.feature

import cc.meteormc.yourmiui.xposed.HookFeature
import cc.meteormc.yourmiui.xposed.ReflectHelper
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.callbacks.XC_LoadPackage

object DisablePause : HookFeature(
    name = "superwallpaper_disable_pause_name",
    description = "superwallpaper_disable_pause_description",
    warning = "superwallpaper_disable_pause_warning",
    testEnvironment = "superwallpaper_disable_pause_test_environment"
) {
    override fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        ReflectHelper.of("com.miui.miwallpaper.basesuperwallpaper.SuperWallpaper", lpparam.classLoader)?.operate {
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
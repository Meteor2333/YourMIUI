package cc.meteormc.yourmiui.app.superwallpaper.hook

import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.app.Hook
import cc.meteormc.yourmiui.helper.ReflectHelper
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.callbacks.XC_LoadPackage

object DisablePause : Hook(
    name = R.string.superwallpaper_disable_pause_name,
    description = R.string.superwallpaper_disable_pause_description,
    warning = R.string.superwallpaper_disable_pause_warning,
    testEnvironment = R.string.superwallpaper_disable_pause_test_environment
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
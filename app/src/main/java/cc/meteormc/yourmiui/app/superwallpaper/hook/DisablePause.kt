package cc.meteormc.yourmiui.app.superwallpaper.hook

import cc.meteormc.yourmiui.app.Hook
import cc.meteormc.yourmiui.helper.ReflectHelper
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.callbacks.XC_LoadPackage

object DisablePause: Hook(
    name = "禁止壁纸暂停",
    description = "解决超级壁纸在锁屏界面仅显示5秒动画的问题，需要在作用域中勾选你所使用的超级壁纸",
    warning = "开启此功能可能导致不必要的性能占用",
    testEnvironment = "所有样式超级壁纸的ALPHA-2.6.555-03232016-ogl-64版本"
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
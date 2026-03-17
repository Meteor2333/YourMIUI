package cc.meteormc.yourmiui.app.packageinstaller.hook

import cc.meteormc.yourmiui.app.Hook
import cc.meteormc.yourmiui.helper.ReflectHelper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

object RemoveAd: Hook(
    name = "去广告",
    description = "去除安装器中的赞助商软件推荐",
    testEnvironment= "新版本疑似已无广告，无法测试"
) {
    override fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        ReflectHelper.of("com.miui.packageInstaller.model.CloudParams", lpparam.classLoader)?.operate {
            declaredConstructors().forEach { ctor -> ctor.hook(object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    setOf(
                        // name: showAdsBefore | type: boolean
                        "showAdsBefore",
                        // name: showAdsAfter | type: boolean
                        "showAdsAfter",
                        // name: singletonAuthShowAdsBefore | type: boolean
                        "singletonAuthShowAdsBefore",
                        // name: singletonAuthShowAdsAfter | type: boolean
                        "singletonAuthShowAdsAfter"
                    ).forEach { field(it)?.set(param.thisObject, false) }
                }
            }) }
        }
    }
}
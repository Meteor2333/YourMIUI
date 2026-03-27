package cc.meteormc.yourmiui.app.packageinstaller.hook

import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.app.Hook
import cc.meteormc.yourmiui.helper.ReflectHelper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

object RemoveAd: Hook(
    name = R.string.packageinstaller_remove_ad_name,
    description = R.string.packageinstaller_remove_ad_description
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
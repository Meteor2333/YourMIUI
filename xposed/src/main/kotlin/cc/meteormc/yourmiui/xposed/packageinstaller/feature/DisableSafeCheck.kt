package cc.meteormc.yourmiui.xposed.packageinstaller.feature

import cc.meteormc.yourmiui.xposed.HookFeature
import cc.meteormc.yourmiui.xposed.ReflectHelper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

object DisableSafeCheck : HookFeature(
    name = "packageinstaller_disable_safe_check_name",
    description = "packageinstaller_disable_safe_check_description",
    warning = "packageinstaller_disable_safe_check_warning",
    testEnvironment = "packageinstaller_disable_safe_check_test_environment"
) {
    override fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        ReflectHelper.of("com.miui.packageInstaller.model.CloudParams", lpparam.classLoader)?.operate {
            // modifier: public | signature: <init>()V
            declaredConstructors().forEach { ctor -> ctor.hook(object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    // name: safeType | type: java.lang.String
                    field("safeType")?.set(param.thisObject, "no_block")
                }
            }) }
        }
    }
}
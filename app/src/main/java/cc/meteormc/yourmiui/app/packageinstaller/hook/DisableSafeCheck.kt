package cc.meteormc.yourmiui.app.packageinstaller.hook

import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.app.Hook
import cc.meteormc.yourmiui.helper.ReflectHelper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage

object DisableSafeCheck : Hook(
    name = R.string.packageinstaller_disable_safe_check_name,
    description = R.string.packageinstaller_disable_safe_check_description,
    warning = R.string.packageinstaller_disable_safe_check_warning,
    testEnvironment = R.string.packageinstaller_disable_safe_check_test_environment
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
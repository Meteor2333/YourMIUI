package cc.meteormc.yourmiui.xposed.packageinstaller.feature

import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature
import de.robv.android.xposed.XC_MethodHook

object DisableSafeCheck : XposedFeature(
    key = "packageinstaller_disable_safe_check",
    nameRes = R.string.feature_packageinstaller_disable_safe_check_name,
    descriptionRes = R.string.feature_packageinstaller_disable_safe_check_description,
    warningRes = R.string.feature_packageinstaller_disable_safe_check_warning,
    testEnvironmentRes = R.string.feature_packageinstaller_disable_safe_check_test_environment
) {
    override fun init() {
        helper("com.miui.packageInstaller.model.CloudParams")?.operate {
            // modifier: public | signature: <init>()V
            constructor()?.hook(object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    // name: safeType | type: java.lang.String
                    field("safeType")?.set(param.thisObject, "no_block")
                }
            })
        }
    }
}
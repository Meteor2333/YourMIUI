package cc.meteormc.yourmiui.xposed.packageinstaller.feature

import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature

object DisableSafeCheck : XposedFeature(
    key = "packageinstaller_disable_safe_check",
    nameRes = R.string.feature_packageinstaller_disable_safe_check_name,
    descriptionRes = R.string.feature_packageinstaller_disable_safe_check_description,
    warningRes = R.string.feature_packageinstaller_disable_safe_check_warning,
    testEnvironmentRes = R.string.feature_packageinstaller_disable_safe_check_test_environment
) {
    override fun init() {
        helper("com.miui.packageInstaller.model.CloudParams") {
            // modifier: public | signature: <init>()V
            constructor()?.hookAfter {
                // name: safeType | type: java.lang.String
                field("safeType")?.set(it.thisObject, "no_block")
            }
        }
    }
}
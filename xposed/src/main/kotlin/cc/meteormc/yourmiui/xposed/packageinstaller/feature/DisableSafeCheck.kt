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
        helper("com.miui.packageInstaller.model.ApkInfo") {
            val operator = helper("com.miui.packageInstaller.model.CloudParams") ?: return@helper
            val storeListedField = operator.field("storeListed") ?: return@helper
            val secureWarningTipField = operator.field("secureWarningTip") ?: return@helper

            // modifier: public final | signature: getCloudParams()Lcom/miui/packageInstaller/model/CloudParams;
            method("getCloudParams")?.hookAfter {
                val result = it.result
                storeListedField[result] = true
                secureWarningTipField[result] = null
                it.result = result
            }
        }
    }
}
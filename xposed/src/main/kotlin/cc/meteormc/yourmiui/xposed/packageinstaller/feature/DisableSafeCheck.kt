package cc.meteormc.yourmiui.xposed.packageinstaller.feature

import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature

object DisableSafeCheck : XposedFeature(
    key = "disable_safe_check",
    nameRes = R.string.feature_packageinstaller_disable_safe_check_name,
    descriptionRes = R.string.feature_packageinstaller_disable_safe_check_description,
    warningRes = R.string.feature_packageinstaller_disable_safe_check_warning,
    testEnvironmentRes = R.string.feature_packageinstaller_disable_safe_check_test_environment
) {
    override fun onLoadPackage() {
        operator("com.miui.packageInstaller.model.ApkInfo") {
            val operator = operator("com.miui.packageInstaller.model.CloudParams") ?: return@operator
            val storeListedField = operator.field("storeListed") ?: return@operator
            val secureWarningTipField = operator.field("secureWarningTip") ?: return@operator

            // modifier: public final | signature: getCloudParams()Lcom/miui/packageInstaller/model/CloudParams;
            method("getCloudParams")?.hookAfter {
                storeListedField[it.result] = true
                secureWarningTipField[it.result] = null
            }
        }
    }
}
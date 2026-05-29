package cc.meteormc.yourmiui.xposed.packageinstaller.feature

import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator

@FeatureRegister(
    Category.PACKAGE_INSTALLER,
    "@string/feature_packageinstaller_disable_safe_check_name",
    "@string/feature_packageinstaller_disable_safe_check_description",
    "@string/feature_packageinstaller_disable_safe_check_warning"
)
@RequiredScope("com.miui.packageinstaller")
object DisableSafeCheck : Feature(
    key = "disable_safe_check",
    nameRes = R.string.feature_packageinstaller_disable_safe_check_name,
    descriptionRes = R.string.feature_packageinstaller_disable_safe_check_description,
    warningRes = R.string.feature_packageinstaller_disable_safe_check_warning,
    testEnvironmentRes = R.string.feature_packageinstaller_disable_safe_check_test_environment
), FeatureHooker {
    override fun hook(packageName: String) {
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
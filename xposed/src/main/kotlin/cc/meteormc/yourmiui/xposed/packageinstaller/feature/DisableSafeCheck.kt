package cc.meteormc.yourmiui.xposed.packageinstaller.feature

import cc.meteormc.yourmiui.xposed.FieldOps
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
        var storeListedField: FieldOps<Any>? = null
        var secureTipField: FieldOps<Any>? = null
        helper("com.miui.packageInstaller.model.CloudParams") {
            // name: storeListed | type: boolean
            storeListedField = field("storeListed")
            // name: secureTipField | type: com.miui.packageInstaller.model.WarningCardInfo
            secureTipField = field("secureWarningTip")
        }

        helper("com.miui.packageInstaller.model.ApkInfo") {
            // modifier: public final | signature: getCloudParams()Lcom/miui/packageInstaller/model/CloudParams;
            method("getCloudParams")?.hookAfter {
                val result = it.result
                storeListedField?.set(result, true)
                secureTipField?.set(result, null)
                it.result = result
            }
        }
    }
}
package cc.meteormc.yourmiui.xposed.packageinstaller

import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.api.data.HookContext
import cc.meteormc.yourmiui.xposed.hookAfter
import cc.meteormc.yourmiui.xposed.reflect

@FeatureRegister(
    Category.PACKAGE_INSTALLER,
    "@string/feature_packageinstaller_disable_safe_check_name",
    "@string/feature_packageinstaller_disable_safe_check_description",
    "@string/feature_packageinstaller_disable_safe_check_warning"
)
@RequiredScope("com.miui.packageinstaller")
object DisableSafeCheck : FeatureHooker {
    override fun hook(context: HookContext) {
        context.reflect("com.miui.packageInstaller.model.ApkInfo") {
            val operator = context.reflect("com.miui.packageInstaller.model.CloudParams") ?: return@reflect
            val storeListedField = operator.field("storeListed") ?: return@reflect
            val secureWarningTipField = operator.field("secureWarningTip") ?: return@reflect

            // modifier: public final | signature: getCloudParams()Lcom/miui/packageInstaller/model/CloudParams;
            method("getCloudParams")?.hookAfter {
                storeListedField[it.result] = true
                secureWarningTipField[it.result] = null
            }
        }
    }
}
package cc.meteormc.yourmiui.xposed.packageinstaller

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.operator

object DisableSafeCheck : Feature() {
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
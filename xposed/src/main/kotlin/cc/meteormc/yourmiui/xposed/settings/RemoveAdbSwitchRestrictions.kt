package cc.meteormc.yourmiui.xposed.settings

import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.xposed.operator

@FeatureRegister(
    Category.SETTINGS,
    "@string/feature_settings_remove_adb_switch_restrictions_name",
    "@string/feature_settings_remove_adb_switch_restrictions_description"
)
@RequiredScope("com.miui.securitycenter")
object RemoveAdbSwitchRestrictions : FeatureHooker {
    override fun hook(packageName: String) {
        operator($$"com.miui.permcenter.install.AdbInstallVerifyActivity$a") {
            // modifier: protected bridge synthetic | signature: doInBackground([Ljava/lang/Object;)Ljava/lang/Object;
            method("doInBackground")?.hookDoNothing()
        }
    }
}
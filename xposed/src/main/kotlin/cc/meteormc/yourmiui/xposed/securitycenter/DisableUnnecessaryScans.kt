package cc.meteormc.yourmiui.xposed.securitycenter

import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.api.data.HookContext
import cc.meteormc.yourmiui.xposed.hookDoNothing
import cc.meteormc.yourmiui.xposed.reflect

@FeatureRegister(
    Category.SECURITY_CENTER,
    "@string/feature_securitycenter_disable_unnecessary_scans_name",
    "@string/feature_securitycenter_disable_unnecessary_scans_description"
)
@RequiredScope("com.miui.securitycenter")
object DisableUnnecessaryScans : FeatureHooker {
    private val unnecessaryScans = setOf(
        "com.miui.securityscan.model.manualitem.PermissionRootModel",
        "com.miui.securityscan.model.system.AutoDownloadModel",
        "com.miui.securityscan.model.system.DevModeModel",
        "com.miui.securityscan.model.system.UsbModel"
    )

    override fun hook(context: HookContext) {
        for (scan in unnecessaryScans) {
            context.reflect(scan) {
                // modifier: public | signature: scan()V
                method("scan")?.hookDoNothing()
            }
        }
    }
}
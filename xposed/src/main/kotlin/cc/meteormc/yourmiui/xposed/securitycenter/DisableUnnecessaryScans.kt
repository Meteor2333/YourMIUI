package cc.meteormc.yourmiui.xposed.securitycenter

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.operator

object DisableUnnecessaryScans : Feature() {
    private val unnecessaryScans = setOf(
        "com.miui.securityscan.model.manualitem.PermissionRootModel",
        "com.miui.securityscan.model.system.AutoDownloadModel",
        "com.miui.securityscan.model.system.DevModeModel",
        "com.miui.securityscan.model.system.UsbModel"
    )

    override fun onLoadPackage() {
        for (scan in unnecessaryScans) {
            operator(scan) {
                // modifier: public | signature: scan()V
                method("scan")?.hookDoNothing()
            }
        }
    }
}
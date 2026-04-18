package cc.meteormc.yourmiui.xposed.securitycenter.feature

import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature

object DisableUnnecessaryScans : XposedFeature(
    key = "securitycenter_disable_unnecessary_scans",
    nameRes = R.string.feature_securitycenter_disable_unnecessary_scans_name,
    descriptionRes = R.string.feature_securitycenter_disable_unnecessary_scans_description,
    testEnvironmentRes = R.string.feature_securitycenter_disable_unnecessary_scans_test_environment
) {
    private val unnecessaryScans = setOf(
        "com.miui.securityscan.model.manualitem.PermissionRootModel",
        "com.miui.securityscan.model.system.AutoDownloadModel",
        "com.miui.securityscan.model.system.DevModeModel",
        "com.miui.securityscan.model.system.UsbModel"
    )

    override fun onLoadPackage() {
        for (scan in unnecessaryScans) {
            helper(scan) {
                // modifier: public | signature: scan()V
                method("scan")?.hookDoNothing()
            }
        }
    }
}
package cc.meteormc.yourmiui.xposed.securitycenter.feature

import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature
import de.robv.android.xposed.XC_MethodReplacement

object DisableMinorScan : XposedFeature(
    nameRes = R.string.feature_securitycenter_disable_minor_scan_name,
    descriptionRes = R.string.feature_securitycenter_disable_minor_scan_description,
    testEnvironmentRes = R.string.feature_securitycenter_disable_minor_scan_test_environment
) {
    private val minorScan = setOf(
        "com.miui.securityscan.model.manualitem.PermissionRootModel",
        "com.miui.securityscan.model.system.AutoDownloadModel",
        "com.miui.securityscan.model.system.DevModeModel",
        "com.miui.securityscan.model.system.UsbModel"
    )

    override fun init() {
        for (scan in minorScan) {
            helper(scan)?.operate {
                // modifier: public | signature: scan()V
                method("scan")?.hook(XC_MethodReplacement.DO_NOTHING)
            }
        }
    }
}
package cc.meteormc.yourmiui.app.securitycenter.hook

import cc.meteormc.yourmiui.R
import cc.meteormc.yourmiui.app.Hook
import cc.meteormc.yourmiui.helper.ReflectHelper
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.callbacks.XC_LoadPackage

object DisableMinorScan: Hook(
    name = R.string.securitycenter_disable_minor_scan_name,
    description = R.string.securitycenter_disable_minor_scan_description,
    testEnvironment = R.string.securitycenter_disable_minor_scan_test_environment,
) {
    private val minorScan = setOf(
        "com.miui.securityscan.model.manualitem.PermissionRootModel",
        "com.miui.securityscan.model.system.AutoDownloadModel",
        "com.miui.securityscan.model.system.DevModeModel",
        "com.miui.securityscan.model.system.UsbModel"
    )

    override fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        for (scan in minorScan) {
            ReflectHelper.of(scan, lpparam.classLoader)?.operate {
                // modifier: public | signature: scan()V
                method("scan")?.hook(XC_MethodReplacement.DO_NOTHING)
            }
        }
    }
}
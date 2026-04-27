package cc.meteormc.yourmiui.xposed.securitycenter

import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.securitycenter.feature.*

object SecurityCenter : XposedScope(
    "com.miui.securitycenter"
) {
    override fun getFeatures(): Iterable<XposedFeature> {
        return listOf(
            DisableAdbInstallAlert,
            DisableCountdownDialog,
            DisableUnnecessaryScans,
            FixTrafficCorrection,
            RemoveAdbSwitchRestrictions
        )
    }
}
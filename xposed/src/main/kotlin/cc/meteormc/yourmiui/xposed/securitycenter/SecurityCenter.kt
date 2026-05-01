package cc.meteormc.yourmiui.xposed.securitycenter

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.xposed.securitycenter.feature.*

object SecurityCenter : Scope(
    "com.miui.securitycenter" to "com.miui.securityscan.MainActivity"
) {
    override fun getFeatures(): List<Feature> {
        return listOf(
            DisableAdbInstallAlert,
            DisableCountdownDialog,
            DisableUnnecessaryScans,
            FixTrafficCorrection,
            RemoveAdbSwitchRestrictions
        )
    }
}
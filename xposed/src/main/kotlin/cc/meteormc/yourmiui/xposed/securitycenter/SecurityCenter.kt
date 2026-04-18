package cc.meteormc.yourmiui.xposed.securitycenter

import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.securitycenter.feature.DisableCountdownDialog
import cc.meteormc.yourmiui.xposed.securitycenter.feature.DisableUnnecessaryScans
import cc.meteormc.yourmiui.xposed.securitycenter.feature.FixTrafficCorrection
import cc.meteormc.yourmiui.xposed.securitycenter.feature.RemoveAdbSwitchRestrictions

object SecurityCenter : XposedScope(
    "com.miui.securitycenter"
) {
    override fun getFeatures(): Iterable<XposedFeature> {
        return listOf(
            DisableCountdownDialog,
            DisableUnnecessaryScans,
            FixTrafficCorrection,
            RemoveAdbSwitchRestrictions
        )
    }
}
package cc.meteormc.yourmiui.xposed.securitycenter

import android.content.ComponentName
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.common.data.RestartMethod
import cc.meteormc.yourmiui.xposed.securitycenter.feature.*

object SecurityCenter : Scope(
    "com.miui.securitycenter"
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

    override fun getRestartMethod() = RestartMethod.ViaComponent(
        ComponentName("com.miui.securitycenter", "com.miui.securityscan.MainActivity")
    )
}
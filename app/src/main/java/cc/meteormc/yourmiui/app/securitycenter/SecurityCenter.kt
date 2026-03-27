package cc.meteormc.yourmiui.app.securitycenter

import cc.meteormc.yourmiui.app.App
import cc.meteormc.yourmiui.app.Hook
import cc.meteormc.yourmiui.app.securitycenter.hook.DisableCountdownDialog
import cc.meteormc.yourmiui.app.securitycenter.hook.DisableMinorScan
import cc.meteormc.yourmiui.app.securitycenter.hook.FixTrafficCorrection

object SecurityCenter: App(
    "com.miui.securitycenter"
) {
    override fun getHooks(): Iterable<Hook> {
        return listOf(
            DisableCountdownDialog,
            DisableMinorScan,
            FixTrafficCorrection
        )
    }
}
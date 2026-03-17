package cc.meteormc.yourmiui.app.securitycenter

import cc.meteormc.yourmiui.app.App
import cc.meteormc.yourmiui.app.Hook
import cc.meteormc.yourmiui.app.securitycenter.hook.DisableCountdownDialog

object SecurityCenter: App(
    "手机管家",
    "com.miui.securitycenter"
) {
    override fun getHooks(): Iterable<Hook> {
        return listOf(
            DisableCountdownDialog
        )
    }
}
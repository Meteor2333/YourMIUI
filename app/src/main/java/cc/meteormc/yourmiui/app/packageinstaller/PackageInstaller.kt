package cc.meteormc.yourmiui.app.packageinstaller

import cc.meteormc.yourmiui.app.App
import cc.meteormc.yourmiui.app.Hook
import cc.meteormc.yourmiui.app.packageinstaller.hook.DisableSafeCheck
import cc.meteormc.yourmiui.app.packageinstaller.hook.RemoveAd

object PackageInstaller: App(
    "com.miui.packageinstaller"
) {
    override fun getHooks(): Iterable<Hook> {
        return listOf(
            DisableSafeCheck,
            RemoveAd
        )
    }
}
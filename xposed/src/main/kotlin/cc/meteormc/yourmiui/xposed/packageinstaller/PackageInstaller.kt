package cc.meteormc.yourmiui.xposed.packageinstaller

import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.packageinstaller.feature.DisableSafeCheck

object PackageInstaller : XposedScope(
    "com.miui.packageinstaller"
) {
    override fun getFeatures(): Iterable<XposedFeature> {
        return listOf(
            DisableSafeCheck
        )
    }
}
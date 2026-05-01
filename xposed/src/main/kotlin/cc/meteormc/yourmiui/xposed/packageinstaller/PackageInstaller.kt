package cc.meteormc.yourmiui.xposed.packageinstaller

import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.packageinstaller.feature.DisableSafeCheck

object PackageInstaller : XposedScope(
    "com.miui.packageinstaller" to null
) {
    override fun getFeatures(): List<XposedFeature> {
        return listOf(
            DisableSafeCheck
        )
    }
}
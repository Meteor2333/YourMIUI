package cc.meteormc.yourmiui.xposed.packageinstaller

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.xposed.packageinstaller.feature.DisableSafeCheck

object PackageInstaller : Scope(
    "com.miui.packageinstaller" to null
) {
    override fun getFeatures(): List<Feature> {
        return listOf(
            DisableSafeCheck
        )
    }
}
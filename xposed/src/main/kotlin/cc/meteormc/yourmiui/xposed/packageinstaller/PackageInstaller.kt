package cc.meteormc.yourmiui.xposed.packageinstaller

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.common.data.RestartMethod
import cc.meteormc.yourmiui.xposed.packageinstaller.feature.DisableSafeCheck

object PackageInstaller : Scope(
    "com.miui.packageinstaller"
) {
    override fun getFeatures(): List<Feature> {
        return listOf(
            DisableSafeCheck
        )
    }

    override fun getRestartMethod() = RestartMethod.DoNothing
}
package cc.meteormc.yourmiui.xposed.settings

import android.content.ComponentName
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.common.data.RestartMethod
import cc.meteormc.yourmiui.xposed.settings.feature.DisableForceNotification
import cc.meteormc.yourmiui.xposed.settings.feature.RemoveMiCloudHeader
import cc.meteormc.yourmiui.xposed.settings.feature.RemoveNewVersionBadge

object Settings : Scope(
    "com.android.settings"
) {
    override fun getFeatures(): List<Feature> {
        return listOf(
            DisableForceNotification,
            RemoveMiCloudHeader,
            RemoveNewVersionBadge
        )
    }

    override fun getRestartMethod() = RestartMethod.ViaComponent(
        ComponentName("com.android.settings", "com.android.settings.MainSettings")
    )
}
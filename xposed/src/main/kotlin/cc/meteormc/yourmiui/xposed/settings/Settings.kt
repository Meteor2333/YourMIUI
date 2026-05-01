package cc.meteormc.yourmiui.xposed.settings

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.xposed.settings.feature.DisableForceNotification
import cc.meteormc.yourmiui.xposed.settings.feature.RemoveNewVersionBadge

object Settings : Scope(
    "com.android.settings" to "com.android.settings.MainSettings"
) {
    override fun getFeatures(): List<Feature> {
        return listOf(
            DisableForceNotification,
            RemoveNewVersionBadge
        )
    }
}
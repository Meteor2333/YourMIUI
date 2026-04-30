package cc.meteormc.yourmiui.xposed.settings

import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.settings.feature.DisableForceNotification
import cc.meteormc.yourmiui.xposed.settings.feature.RemoveNewVersionBadge

object Settings : XposedScope(
    "com.android.settings" to "com.android.settings.MainSettings"
) {
    override fun getFeatures(): Iterable<XposedFeature> {
        return listOf(
            DisableForceNotification,
            RemoveNewVersionBadge
        )
    }
}
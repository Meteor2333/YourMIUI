package cc.meteormc.yourmiui.xposed.settings

import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.settings.feature.RemoveNewVersionBadge

object Settings : XposedScope(
    "com.android.settings"
) {
    override fun getFeatures(): Iterable<XposedFeature> {
        return listOf(
            RemoveNewVersionBadge
        )
    }
}
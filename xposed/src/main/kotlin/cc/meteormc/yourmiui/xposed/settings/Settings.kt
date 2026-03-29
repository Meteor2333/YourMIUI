package cc.meteormc.yourmiui.xposed.settings

import cc.meteormc.yourmiui.core.Feature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.settings.feature.RemoveNewVersionTag

object Settings : XposedScope(
    "com.android.settings"
) {
    override fun getFeatures(): Iterable<Feature> {
        return listOf(
            RemoveNewVersionTag
        )
    }
}
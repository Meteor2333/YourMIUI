package cc.meteormc.yourmiui.xposed.contentextension

import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.contentextension.feature.FixLinkHandling

object ContentExtension : XposedScope(
    "com.miui.contentextension" to "com.miui.contentextension.setting.activity.MainSettingsActivity"
) {
    override fun getFeatures(): Iterable<XposedFeature> {
        return listOf(
            FixLinkHandling
        )
    }
}
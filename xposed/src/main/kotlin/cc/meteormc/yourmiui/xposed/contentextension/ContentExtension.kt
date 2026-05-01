package cc.meteormc.yourmiui.xposed.contentextension

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.xposed.contentextension.feature.FixLinkHandling

object ContentExtension : Scope(
    "com.miui.contentextension" to "com.miui.contentextension.setting.activity.MainSettingsActivity"
) {
    override fun getFeatures(): List<Feature> {
        return listOf(
            FixLinkHandling
        )
    }
}
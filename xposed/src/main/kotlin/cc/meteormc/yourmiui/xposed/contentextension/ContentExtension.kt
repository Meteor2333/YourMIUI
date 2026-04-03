package cc.meteormc.yourmiui.xposed.contentextension

import cc.meteormc.yourmiui.core.Feature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.contentextension.feature.FixLinkOpen

object ContentExtension : XposedScope(
    "com.miui.contentextension"
) {
    override fun getFeatures(): Iterable<Feature> {
        return listOf(
            FixLinkOpen
        )
    }
}
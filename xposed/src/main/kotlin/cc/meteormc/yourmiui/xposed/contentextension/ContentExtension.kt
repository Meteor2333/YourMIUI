package cc.meteormc.yourmiui.xposed.contentextension

import cc.meteormc.yourmiui.core.Feature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.contentextension.feature.FixLinkHandling

object ContentExtension : XposedScope(
    "com.miui.contentextension"
) {
    override fun getFeatures(): Iterable<Feature> {
        return listOf(
            FixLinkHandling
        )
    }
}
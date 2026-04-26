package cc.meteormc.yourmiui.xposed.mirror

import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.mirror.feature.DisableClipTip

object Mirror : XposedScope(
    "com.xiaomi.mirror"
) {
    override fun getFeatures(): Iterable<XposedFeature> {
        return listOf(
            DisableClipTip
        )
    }
}
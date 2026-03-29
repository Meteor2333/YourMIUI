package cc.meteormc.yourmiui.xposed.market

import cc.meteormc.yourmiui.core.Feature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.market.feature.HideTab
import cc.meteormc.yourmiui.xposed.market.feature.RemoveAd

object Market : XposedScope(
    "com.xiaomi.market"
) {
    override fun getFeatures(): Iterable<Feature> {
        return listOf(
            HideTab,
            RemoveAd
        )
    }
}
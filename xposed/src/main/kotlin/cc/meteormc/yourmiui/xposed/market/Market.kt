package cc.meteormc.yourmiui.xposed.market

import cc.meteormc.yourmiui.core.Feature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.market.feature.HideTabs
import cc.meteormc.yourmiui.xposed.market.feature.RemoveAds

object Market : XposedScope(
    "com.xiaomi.market"
) {
    override fun getFeatures(): Iterable<Feature> {
        return listOf(
            HideTabs,
            RemoveAds
        )
    }
}
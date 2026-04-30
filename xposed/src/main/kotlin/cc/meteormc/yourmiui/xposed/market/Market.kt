package cc.meteormc.yourmiui.xposed.market

import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.market.feature.HideTabs
import cc.meteormc.yourmiui.xposed.market.feature.RemoveAds

object Market : XposedScope(
    "com.xiaomi.market" to "com.xiaomi.market.ui.MarketTabActivity"
) {
    override fun getFeatures(): Iterable<XposedFeature> {
        return listOf(
            HideTabs,
            RemoveAds
        )
    }
}
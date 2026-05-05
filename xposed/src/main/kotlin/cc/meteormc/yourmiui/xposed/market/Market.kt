package cc.meteormc.yourmiui.xposed.market

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.xposed.market.feature.DisableNotificationRecall
import cc.meteormc.yourmiui.xposed.market.feature.HideTabs
import cc.meteormc.yourmiui.xposed.market.feature.RemoveAds

object Market : Scope(
    "com.xiaomi.market" to "com.xiaomi.market.ui.MarketTabActivity"
) {
    override fun getFeatures(): List<Feature> {
        return listOf(
            DisableNotificationRecall,
            HideTabs,
            RemoveAds
        )
    }
}
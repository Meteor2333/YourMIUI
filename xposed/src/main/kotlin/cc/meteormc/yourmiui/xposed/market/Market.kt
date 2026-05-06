package cc.meteormc.yourmiui.xposed.market

import android.content.ComponentName
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.common.data.RestartMethod
import cc.meteormc.yourmiui.xposed.market.feature.DisableNotificationRecall
import cc.meteormc.yourmiui.xposed.market.feature.RemoveAds

object Market : Scope(
    "com.xiaomi.market"
) {
    override fun getFeatures(): List<Feature> {
        return listOf(
            DisableNotificationRecall,
            RemoveAds
        )
    }

    override fun getRestartMethod() = RestartMethod.ViaComponent(
        ComponentName("com.xiaomi.market", "com.xiaomi.market.ui.MarketTabActivity")
    )
}
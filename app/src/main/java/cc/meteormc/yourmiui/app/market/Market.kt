package cc.meteormc.yourmiui.app.market

import cc.meteormc.yourmiui.app.App
import cc.meteormc.yourmiui.app.Hook
import cc.meteormc.yourmiui.app.market.hook.HideTab
import cc.meteormc.yourmiui.app.market.hook.RemoveAd

object Market : App(
    "com.xiaomi.market"
) {
    override fun getHooks(): Iterable<Hook> {
        return listOf(
            HideTab,
            RemoveAd
        )
    }
}
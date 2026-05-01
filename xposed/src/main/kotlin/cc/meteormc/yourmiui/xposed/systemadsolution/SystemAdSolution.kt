package cc.meteormc.yourmiui.xposed.systemadsolution

import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.systemadsolution.feature.RemoveSplashAds

object SystemAdSolution : XposedScope(
    "com.miui.systemAdSolution" to null,
    // 智能服务？ 广告服务！
    nameRes = R.string.scope_systemadsolution
) {
    override fun getFeatures(): List<XposedFeature> {
        return listOf(
            RemoveSplashAds
        )
    }
}
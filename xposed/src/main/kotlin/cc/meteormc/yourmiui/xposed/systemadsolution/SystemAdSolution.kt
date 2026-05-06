package cc.meteormc.yourmiui.xposed.systemadsolution

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.common.data.RestartMethod
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.systemadsolution.feature.RemoveSplashAds

object SystemAdSolution : Scope(
    "com.miui.systemAdSolution",
    // 智能服务？ 广告服务！
    nameRes = R.string.scope_systemadsolution
) {
    override fun getFeatures(): List<Feature> {
        return listOf(
            RemoveSplashAds
        )
    }

    override fun getRestartMethod() = RestartMethod.DoNothing
}
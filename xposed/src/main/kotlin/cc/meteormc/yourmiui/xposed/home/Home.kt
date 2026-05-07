package cc.meteormc.yourmiui.xposed.home

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.common.data.RestartMethod
import cc.meteormc.yourmiui.xposed.home.feature.DisableTodayRecommend
import cc.meteormc.yourmiui.xposed.home.feature.HideFolderScrollBar

object Home : Scope(
    "com.miui.home"
) {
    override fun getFeatures(): List<Feature> {
        return listOf(
            DisableTodayRecommend,
            HideFolderScrollBar
        )
    }

    override fun getRestartMethod() = RestartMethod.DoNothing
}
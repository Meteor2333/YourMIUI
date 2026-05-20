package cc.meteormc.yourmiui.xposed.ui

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.operator

object DisableTodayRecommend : Feature() {
    override fun onLoadPackage(packageName: String) {
        operator("com.miui.home.launcher.Folder") {
            // modifier: public | signature: showRecommendAppsSwitch(ZZ)V
            method("showRecommendAppsSwitch")?.hookDoNothing()
        }

        operator("com.miui.home.launcher.commercial.recommend.RecommendController") {
            // modifier: public | signature: canRecommendScreenShow()Z
            method("canRecommendScreenShow")?.hookResult(false)

        }
    }
}
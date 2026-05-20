package cc.meteormc.yourmiui.xposed.home.feature

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.xposed.R
import cc.meteormc.yourmiui.xposed.operator

object DisableTodayRecommend : Feature(
    key = "disable_today_recommend",
    nameRes = R.string.feature_home_disable_today_recommend_name,
    descriptionRes = R.string.feature_home_disable_today_recommend_description,
    testEnvironmentRes = R.string.feature_home_disable_today_recommend_test_environment
) {
    override fun onLoadPackage() {
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
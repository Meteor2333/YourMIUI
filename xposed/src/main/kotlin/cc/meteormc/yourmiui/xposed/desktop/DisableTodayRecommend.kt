package cc.meteormc.yourmiui.xposed.desktop

import cc.meteormc.yourmiui.api.Category
import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import cc.meteormc.yourmiui.api.data.HookContext
import cc.meteormc.yourmiui.xposed.hookDoNothing
import cc.meteormc.yourmiui.xposed.hookResult
import cc.meteormc.yourmiui.xposed.reflect

@FeatureRegister(
    Category.DESKTOP,
    "@string/feature_desktop_disable_today_recommend_name",
    "@string/feature_desktop_disable_today_recommend_description"
)
@RequiredScope("com.miui.home")
object DisableTodayRecommend : FeatureHooker {
    override fun hook(context: HookContext) {
        context.reflect("com.miui.home.launcher.Folder") {
            // modifier: public | signature: showRecommendAppsSwitch(ZZ)V
            method("showRecommendAppsSwitch")?.hookDoNothing()
        }

        context.reflect("com.miui.home.launcher.commercial.recommend.RecommendController") {
            // modifier: public | signature: canRecommendScreenShow()Z
            method("canRecommendScreenShow")?.hookResult(false)
        }
    }
}
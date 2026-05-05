package cc.meteormc.yourmiui.xposed.systemui

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.xposed.systemui.feature.DisableForceNotification
import cc.meteormc.yourmiui.xposed.systemui.feature.EditGxzwQuickOpen
import cc.meteormc.yourmiui.xposed.systemui.feature.FixSplashScreen
import cc.meteormc.yourmiui.xposed.systemui.feature.HideStatusBarIcons

object SystemUI : Scope(
    "com.android.systemui" to null
) {
    override fun getFeatures(): List<Feature> {
        return listOf(
            DisableForceNotification,
            EditGxzwQuickOpen,
            FixSplashScreen,
            HideStatusBarIcons
        )
    }
}
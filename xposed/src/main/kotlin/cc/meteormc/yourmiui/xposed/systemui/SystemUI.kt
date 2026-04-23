package cc.meteormc.yourmiui.xposed.systemui

import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.systemui.feature.EditGxzwQuickOpen
import cc.meteormc.yourmiui.xposed.systemui.feature.FixSplashScreen

object SystemUI : XposedScope(
    "com.android.systemui"
) {
    override fun getFeatures(): Iterable<XposedFeature> {
        return listOf(
            EditGxzwQuickOpen,
            FixSplashScreen
        )
    }
}
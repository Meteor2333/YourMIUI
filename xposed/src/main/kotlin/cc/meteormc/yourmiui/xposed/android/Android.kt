package cc.meteormc.yourmiui.xposed.android

import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.android.feature.BlockProcessKill

object Android : XposedScope(
    "android"
) {
    override fun getFeatures(): Iterable<XposedFeature> {
        return listOf(
            BlockProcessKill
        )
    }
}
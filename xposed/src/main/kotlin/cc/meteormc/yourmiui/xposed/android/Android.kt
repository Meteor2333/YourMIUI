package cc.meteormc.yourmiui.xposed.android

import cc.meteormc.yourmiui.core.Feature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.android.feature.BlockKillProcess

object Android : XposedScope(
    "android"
) {
    override fun getFeatures(): Iterable<Feature> {
        return listOf(
            BlockKillProcess
        )
    }
}
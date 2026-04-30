package cc.meteormc.yourmiui.xposed.android

import cc.meteormc.yourmiui.xposed.XposedFeature
import cc.meteormc.yourmiui.xposed.XposedScope
import cc.meteormc.yourmiui.xposed.android.feature.BlockProcessKill
import cc.meteormc.yourmiui.xposed.android.feature.DisableForceNotification

object Android : XposedScope(
    "android" to null,
    restartable = false
) {
    override fun getFeatures(): Iterable<XposedFeature> {
        return listOf(
            BlockProcessKill,
            DisableForceNotification
        )
    }
}
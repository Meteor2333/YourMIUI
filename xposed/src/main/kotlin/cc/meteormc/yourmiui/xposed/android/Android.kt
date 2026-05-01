package cc.meteormc.yourmiui.xposed.android

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.xposed.android.feature.BlockProcessKill
import cc.meteormc.yourmiui.xposed.android.feature.DisableForceNotification

object Android : Scope(
    "android" to null,
    restartable = false
) {
    override fun getFeatures(): List<Feature> {
        return listOf(
            BlockProcessKill,
            DisableForceNotification
        )
    }
}
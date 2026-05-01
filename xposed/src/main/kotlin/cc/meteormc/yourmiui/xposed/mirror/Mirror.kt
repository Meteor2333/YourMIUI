package cc.meteormc.yourmiui.xposed.mirror

import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.xposed.mirror.feature.DisableClipTip

object Mirror : Scope(
    "com.xiaomi.mirror" to "com.xiaomi.mirror.settings.SettingsHomeActivity"
) {
    override fun getFeatures(): List<Feature> {
        return listOf(
            DisableClipTip
        )
    }
}
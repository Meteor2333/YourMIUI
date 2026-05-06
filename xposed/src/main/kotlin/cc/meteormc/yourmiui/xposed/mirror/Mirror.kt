package cc.meteormc.yourmiui.xposed.mirror

import android.content.ComponentName
import cc.meteormc.yourmiui.common.Feature
import cc.meteormc.yourmiui.common.Scope
import cc.meteormc.yourmiui.common.data.RestartMethod
import cc.meteormc.yourmiui.xposed.mirror.feature.DisableClipTip

object Mirror : Scope(
    "com.xiaomi.mirror"
) {
    override fun getFeatures(): List<Feature> {
        return listOf(
            DisableClipTip
        )
    }

    override fun getRestartMethod() = RestartMethod.ViaComponent(
        ComponentName("com.xiaomi.mirror", "com.xiaomi.mirror.settings.SettingsHomeActivity")
    )
}